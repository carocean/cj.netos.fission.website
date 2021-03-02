package cj.netos.fission.service;

import cj.netos.fission.*;
import cj.netos.fission.model.*;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.ultimate.util.StringUtil;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import redis.clients.jedis.JedisCluster;

import java.util.*;

@CjService(name = "personInfoService")
public class PersonInfoService implements IPersonInfoService, IndexPoolConstants {
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    ITagService tagService;
    @CjServiceRef
    IAreaService areaService;
    @CjServiceRef
    ICashierBalanceService cashierBalanceService;
    @CjServiceRef
    ICashierService cashierService;
    @CjServiceRef
    IAttachmentService attachmentService;
    @CjServiceRef
    IRecommendedService recommendedService;

    @CjServiceRef(refByName = "@.redis.cluster")
    JedisCluster jedisCluster;
    @CjServiceSite
    IServiceSite site;

    @Override
    public long totalPerson() {
        return personService.total();
    }

    @Override
    public PersonInfo getInfo(String unionid) {
        Person person = personService.get(unionid);
        if (person == null) {
            return null;
        }
        return loadPersonInfo(person);
    }

    @Override
    public PersonInfo loadPersonInfo(Person person) {
        if (person == null) {
            return null;
        }
        String unionid = person.getId();
        Cashier cashier = cashierService.getCashier(unionid);
        List<Tag> propTags = tagService.listPropTag(unionid);
        List<LimitTag> limitTags = tagService.listLimitTag(unionid);
        List<String> payerTagIds = new ArrayList<>();
        List<String> payeeTagIds = new ArrayList<>();
        for (LimitTag limitTag : limitTags) {
            switch (limitTag.getDirect()) {
                case "payer":
                    payerTagIds.add(limitTag.getTag());
                    break;
                case "payee":
                    payeeTagIds.add(limitTag.getTag());
                    break;
            }
        }
        List<Tag> payerTags = tagService.listTagIn(payerTagIds);
        List<Tag> payeeTags = tagService.listTagIn(payeeTagIds);
        Area area = areaService.getLimitArea(unionid);
        Area payerArea = null;
        Area payeeArea = null;
        if (area != null) {
            switch (area.getDirect()) {
                case "payer":
                    payerArea = area;
                    break;
                case "payee":
                    payeeArea = area;
                    break;
            }
        }
        CashierBalance balance = cashierBalanceService.getBalance(unionid);
        if (balance == null) {
            balance = new CashierBalance();
            balance.setPerson(unionid);
            balance.setBalance(0L);
        }
        PersonInfo info = new PersonInfo();
        info.setPerson(person);
        info.setCashier(cashier);
        info.setPropTags(propTags);
        info.setPayerArea(payerArea);
        info.setPayerTags(payerTags);
        info.setPayeeArea(payeeArea);
        info.setPayeeTags(payeeTags);
        info.setBalance(balance.getBalance());
        return info;
    }

    @Override
    public List<PersonInfo> searchByDefault(PersonInfo current, int limit, long skip) {
        //维度之间也取并集
        Person person = current.getPerson();
        Map<String, Boolean> idMap = new LinkedHashMap<>();

        Collection<String> ids = null;
        Map<String, Object> tuples = new HashMap<>();//persons的元组，含有distance
        if (person.getLocation() != null) {
            ids = searchIdByAround(tuples, person, person.getLocation(), "20000"/*默认20公里*/, limit, skip);
            for (String id : ids) {
                idMap.put(id, true);
            }
        }
        //匹配属性标签
        List<Tag> payeeTags = current.getPropTags();
        for (Tag tag : payeeTags) {
            String key = String.format("%s.%s", _KEY_POOL_TAG, tag.getId());
            ids = jedisCluster.zrange(key, skip, limit + skip);
            for (String id : ids) {
                idMap.put(id, true);
            }
        }
        //匹配常规
        String key = _KEY_POOL_NORMAL;
        ids = jedisCluster.zrange(key, skip, limit + skip);
        for (String id : ids) {
            idMap.put(id, true);
        }
        List<String> pids = new ArrayList<>();
        pids.addAll(idMap.keySet());
        recommendedService.visitIdList(current.getPerson().getId(), pids);
        List<PersonInfo> infos = new ArrayList<>();
        for (String id : pids) {
            if (id.equals(person.getId())) {
                continue;
            }
            Person p = personService.get(id);
            if (p == null) {
                continue;
            }
            PersonInfo info = loadPersonInfo(p);
            if (cannotRecommend(info)) {
                continue;
            }
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<PersonInfo> searchByPayeeTags(PersonInfo current, int limit, long skip) {//求并集
        List<Tag> payeeTags = current.getPayeeTags();
        Map<String, Boolean> idMap = new LinkedHashMap<>();
        for (Tag tag : payeeTags) {
            String key = String.format("%s.%s", _KEY_POOL_TAG, tag.getId());
            Set<String> ids = jedisCluster.zrange(key, skip, limit + skip);
            for (String id : ids) {
                idMap.put(id, true);
            }
        }
        List<String> ids = new ArrayList<>();
        ids.addAll(idMap.keySet());
        recommendedService.visitIdList(current.getPerson().getId(), ids);
        List<PersonInfo> infos = new ArrayList<>();
        for (String id : ids) {
            if (id.equals(current.getPerson().getId())) {
                continue;
            }
            Person p = personService.get(id);
            PersonInfo info = loadPersonInfo(p);
            if (cannotRecommend(info)) {
                continue;
            }
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<PersonInfo> searchByArea(PersonInfo current, int limit, long skip) {
        Area area = current.getPayeeArea();
        Person person = current.getPerson();
        Collection<String> ids = null;
        Map<String, Object> tuples = new HashMap<>();//persons的元组，含有distance
        switch (area.getAreaType()) {
            case "around":
                ids = searchIdByAround(tuples, person, person.getLocation(), area.getAreaCode(), limit, skip);
                break;
            case "province":
                ids = searchIdByProvince(area.getAreaCode(), limit, skip);
                break;
            case "city":
                ids = searchIdByCity(area.getAreaCode(), limit, skip);
                break;
            default:
                ids = new ArrayList<>();
                break;
        }
        List<String> rids = new ArrayList<>();
        rids.addAll(ids);
        recommendedService.visitIdList(current.getPerson().getId(), rids);
        List<PersonInfo> infos = new ArrayList<>();
        for (String id : rids) {
            if (id.equals(person.getId())) {
                continue;
            }
            Map<String, Object> tuple = (Map<String, Object>) tuples.get(id);
            if (tuple != null) {
                Person p = Person.load(tuple);
                PersonInfo info = loadPersonInfo(p);
                infos.add(info);
                continue;
            }
            Person p = personService.get(id);
            PersonInfo info = loadPersonInfo(p);
            if (cannotRecommend(info)) {
                continue;
            }
            infos.add(info);
        }
        return infos;
    }

    @Override
    public List<PersonInfo> searchByAreaAndPayeeTags(PersonInfo current, int limit, long skip) {
        Area area = current.getPayeeArea();
        Person person = current.getPerson();
        Collection<String> ids = null;
        Map<String, Object> tuples = new HashMap<>();//persons的元组，含有distance
        switch (area.getAreaType()) {
            case "around":
                ids = searchIdByAround(tuples, person, person.getLocation(), area.getAreaCode(), limit, skip);
                break;
            case "province":
                ids = searchIdByProvince(area.getAreaCode(), limit, skip);
                break;
            case "city":
                ids = searchIdByCity(area.getAreaCode(), limit, skip);
                break;
            default:
                ids = new ArrayList<>();
                break;
        }
        List<Tag> payeeTags = current.getPayeeTags();
        //ids是否在被支付人兴趣标签集合中存在？如果在所有PayeeTags的集合中不存在，则没有交集，则从ids集中移除
        List<String> rids = new ArrayList<>();//交集
        for (String id : ids) {
            //只要有一个集合中存在则可
            for (Tag tag : payeeTags) {
                String key = String.format("%s.%s", _KEY_POOL_TAG, tag.getId());
                Long rank = jedisCluster.zrank(key, id);//如果返回为null表示成员不存在
                if (rank != null) {//只要有一个存在就为交集
                    rids.add(id);
                    break;
                }
            }
        }
        recommendedService.visitIdList(current.getPerson().getId(), rids);
        List<PersonInfo> infos = new ArrayList<>();

        for (String id : rids) {
            if (id.equals(person.getId())) {
                continue;
            }
            Map<String, Object> tuple = (Map<String, Object>) tuples.get(id);
            if (tuple != null) {
                Person p = Person.load(tuple);
                PersonInfo info = loadPersonInfo(p);
                infos.add(info);
                continue;
            }
            Person p = personService.get(id);
            PersonInfo info = loadPersonInfo(p);
            if (info==null||cannotRecommend(info)) {
                continue;
            }
            infos.add(info);
        }
        return infos;
    }

    boolean cannotRecommend(PersonInfo info) {
        if (info.getCashier()==null||info.getCashier().getState() == 1) {
            return true;
        }
        String openedAmount = site.getProperty("recommender.user.opened.amount");
        if (StringUtil.isEmpty(openedAmount)) {
            openedAmount = "60";
        }
        long openedAmountLong = Long.valueOf(openedAmount);
        return info.getBalance() < openedAmountLong;
    }

    private Collection<String> searchIdByProvince(String provinceCode, int limit, long skip) {
        String key = String.format("%s.%s", _KEY_POOL_AREA_PROVINCE, provinceCode);
        Set<String> set = jedisCluster.zrange(key, skip, limit + skip);
        return set;
    }

    private Collection<String> searchIdByCity(String cityCode, int limit, long skip) {
        String key = String.format("%s.%s", _KEY_POOL_AREA_CITY, cityCode);
        Set<String> set = jedisCluster.zrange(key, skip, limit + skip);
        return set;
    }

    private Collection<String> searchIdByAround(Map<String, Object> tuples, Person currPerson, LatLng location, String radius, int limit, long skip) {
/*
        - 先搜附近用户，再去除停业的，再去除余额不足的
         */
        AggregateIterable<Document> documents = personService.findInAround(location, radius, limit, skip);
        List<String> unionIds = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> tuple = (Map<String, Object>) document.get("tuple");
            double distance = (double) tuple.get("distance");
            String id = (String) tuple.get("id");
            tuples.put(id, tuple);
            Area area = areaService.getLimitArea(id);//对方是否限定了区域
            if (area == null) {
                unionIds.add(id);
                continue;
            }
            switch (area.getAreaType()) {
                case "around":
                    if (StringUtil.isEmpty(area.getAreaCode())) {
                        unionIds.add(id);
                        break;
                    }
                    double oDistance = Double.valueOf(area.getAreaCode());
                    if (distance <= oDistance) {//在对方的设定范围内
                        unionIds.add(id);
                    }
                    break;
                case "province":
                    if (!StringUtil.isEmpty(area.getAreaCode()) && area.getAreaCode().equals(currPerson.getProvinceCode())) {//要求同省
                        unionIds.add(id);
                    }
                    break;
                case "city":
                    String cityFull = String.format("%s·%s", currPerson.getProvinceCode(), currPerson.getCityCode());
                    if (!StringUtil.isEmpty(area.getAreaCode()) && area.getAreaCode().equals(cityFull)) {//要求同市
                        unionIds.add(id);
                    }
                    break;
                case "district":
                    String districtFull = String.format("%s·%s·%s", currPerson.getProvinceCode(), currPerson.getCityCode(), currPerson.getDistrictCode());
                    if (!StringUtil.isEmpty(area.getAreaCode()) && area.getAreaCode().equals(districtFull)) {//要求同区县
                        unionIds.add(id);
                    }
                    break;
                default:
                    unionIds.add(id);
                    break;
            }
        }

        unionIds = cashierService.listByRuning(unionIds);
        String openedAmount = site.getProperty("recommender.user.opened.amount");
        if (StringUtil.isEmpty(openedAmount)) {
            openedAmount = "60";
        }
        unionIds = cashierBalanceService.listGreaterThan(unionIds, Long.valueOf(openedAmount)/*只要大于6毛钱视为开放，就可推荐给他人了，*/);

        return unionIds;
    }

    @Override
    public Attachment getAttachment(String id) {
        return attachmentService.getInfo(id);
    }
}
