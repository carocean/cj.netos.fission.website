package cj.netos.fission.service;

import cj.netos.fission.IPersonInfoService;
import cj.netos.fission.IRandRecommendService;
import cj.netos.fission.IRecommendedService;
import cj.netos.fission.IRecommenderService;
import cj.netos.fission.model.*;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.util.StringUtil;

import java.util.*;
import java.util.function.Predicate;

@CjService(name = "recommenderService")
public class RecommenderService implements IRecommenderService {
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    IRecommendedService recommendedService;
    @CjServiceRef
    IRandRecommendService randRecommendService;

    @Override
    public Collection<PersonInfo> recommend(String unionid, int count) throws CircuitException {
        PersonInfo personInfo = personInfoService.getInfo(unionid);
        //如果有被支付人的地域条件，则先以被支付人的地域条件在用户库中搜与之匹配的地域用户，得集合A
        //如果有被支付人的兴趣标签，则按被支付人的兴趣标签在（如果A非空则在A中搜，否则在用户库中搜）中搜与之匹配的属性标签用户，得集合B
        //如果B为空，则按被支付人的属性兴趣标签在用户库中搜与之匹配的属性标签用户，得集合B
        //再以B集合中用户的支付人地域条件匹配当前用户的地域条件，如果不匹配则从B中移除
        //再以B集合中用户的支付人兴趣标签匹配当前用户的属性兴趣标签，如果不匹配则从B中移除
        //再去除B集合中已推荐给被支付人的用户
        //返回B集合
        //可以循环以装满count，如果装到person表的总数量仍未满则退出循环
        Map<String, PersonInfo> unrecommendeds = new HashMap<>();
        long totalPerson = personInfoService.totalPerson();
        if (totalPerson == 0) {
            return unrecommendeds.values();
        }
        long skip = 0;
        int timers = 1;
        int sizeOnTimes = 0;
        while (timers <= totalPerson && skip < count && skip < totalPerson) {
            boolean isProcess = doArea(personInfo, unrecommendeds, count, skip);
            if (!isProcess) {//如果没有处理则按被支付人的兴趣标签条件来搜索，如果有的话
                isProcess = doPayeeTags(personInfo, unrecommendeds, count, skip);
            }
            if (!isProcess) {//如果没有处理则按被支付人的兴趣标签属性来搜索，如果有的话
                isProcess = doPropTags(personInfo, unrecommendeds, count, skip);
            }
            //没有搜索任何限制条件，则随机取
            if (!isProcess || unrecommendeds.isEmpty()) {//||unrecommendeds.isEmpty() 如果想要设置了被支付人兴趣标签虽然没有记录但仍想有推荐结果，就随机取一些
                doNoneCondition(personInfo, unrecommendeds, count);
            }
            sizeOnTimes = unrecommendeds.size();
            skip += sizeOnTimes;
            filterPayerCashier(personInfo, unrecommendeds);//过滤掉已停业的且余额小于10元的
            filterPayerLimit(personInfo, unrecommendeds);//过滤掉支付人限制
            filterRecommendeds(personInfo, unrecommendeds);//过滤掉已推荐过的
            CJSystem.logging().info(getClass(), String.format("用户:%s 推荐引擎循环搜索次数:%s 本次偏移：%s 检索用户数：%s", personInfo.getPerson().getNickName(), timers, skip, sizeOnTimes));
            timers++;
        }
        CJSystem.logging().info(getClass(), String.format("用户:%s 最终推荐数:%s 共搜索记录数：%s 用户库总数：%s", personInfo.getPerson().getNickName(), unrecommendeds.size(), skip, totalPerson));
        return unrecommendeds.values();

    }

    private void filterPayerCashier(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds) {
        String ids[] = unrecommendeds.keySet().toArray(new String[0]);
        for (String id : ids) {
            PersonInfo info = unrecommendeds.get(id);
            if (info == null) {
                continue;
            }
            if (info.getCashier().getState() == 1 || info.getBalance() < info.getCashier().getCacAverage()*2/*只要大于均值视为开放，就可推荐给他人了，如果设得太高，会使参与的用户变得太少*/) {
                unrecommendeds.remove(id);
            }
        }
    }

    private void filterPayerLimit(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds) {
        String ids[] = unrecommendeds.keySet().toArray(new String[0]);
        for (String id : ids) {
            PersonInfo info = unrecommendeds.get(id);
            if (info == null) {
                continue;
            }
            Area payerArea = info.getPayerArea();
            if (payerArea != null) {
                Person payerPerson = info.getPerson();
                Person payeePerson = personInfo.getPerson();
                switch (payerArea.getAreaType()) {
                    case "around":
                        if ( payerPerson.getLocation() == null) {
                            break;
                        }
                        if ( payeePerson.getLocation() == null) {
                            unrecommendeds.remove(id);
                            break;
                        }
                        double distance = Utils.getDistance(payerPerson.getLocation(), payeePerson.getLocation());
                        double radius = Double.valueOf(payerArea.getAreaCode());
                        if (distance > radius) {
                            unrecommendeds.remove(id);
                        }
                        break;
                    case "province":
                        if (StringUtil.isEmpty(payeePerson.getProvinceCode()) || !payeePerson.getProvinceCode().equals(payerArea.getAreaCode())) {
                            unrecommendeds.remove(id);
                        }
                        break;
                    case "city":
                        String cityCodeFull=String.format("%s·%s",payeePerson.getProvinceCode(),payeePerson.getCityCode());
                        if (!cityCodeFull.equals(payerArea.getAreaCode())) {
                            unrecommendeds.remove(id);
                        }
                        break;
                }
            }
            List<Tag> payerTags = info.getPayerTags();
            List<Tag> payeeTags = personInfo.getPropTags();
            if (!payerTags.isEmpty() && payeeTags.isEmpty()) {
                List<String> orgin = new ArrayList<>();
                for (Tag tag : payerTags) {
                    orgin.add(tag.getId());
                }
                List<String> payeeTagIds = new ArrayList<>();
                for (Tag tag : payeeTags) {
                    payeeTagIds.add(tag.getId());
                }
                orgin.retainAll(payeeTagIds);
                if (orgin.isEmpty()) {
                    unrecommendeds.remove(id);
                }
            }
        }
    }

    private void filterRecommendeds(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds) {
        List<String> pids = new ArrayList<>();
        for (PersonInfo info : unrecommendeds.values()) {
            pids.add(info.getPerson().getId());
        }
        List<String> existsIds = recommendedService.listIncludeIds(pids);
        for (String id : existsIds) {
            unrecommendeds.remove(id);
        }
    }

    private void doNoneCondition(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds, int count) throws CircuitException {
        String personId = personInfo.getPerson().getId();
        List<Person> personList = randRecommendService.randPersons(count);
        for (Person person : personList) {
            if (personId.equals(person.getId())) {
                continue;
            }
            PersonInfo info = personInfoService.loadPersonInfo(person);
            unrecommendeds.put(info.getPerson().getId(), info);
        }

    }

    private boolean doPropTags(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds, int count, long skip) {
        List<Tag> propTags = personInfo.getPropTags();
        if (propTags.isEmpty()) {
            return false;
        }
        List<String> tagIds = new ArrayList<>();
        for (Tag tag : propTags) {
            String tagid = tag.getId();
            if (!StringUtil.isEmpty(tag.getOpposite())) {
                tagid = tag.getOpposite();
            }
            tagIds.add(tagid);
        }
        Set<String> personIds = personInfoService.searchPersonInPropTagsByPage(personInfo.getPerson().getId(), tagIds, count, skip);
        for (String unionid : personIds) {
            PersonInfo info = personInfoService.getInfo(unionid);
            if (info == null) {
                continue;
            }
            unrecommendeds.put(info.getPerson().getId(), info);
        }
        return true;
    }

    private boolean doPayeeTags(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds, int count, long skip) {
        List<Tag> payeeTags = personInfo.getPayeeTags();
        if (payeeTags.isEmpty()) {
            return false;
        }
        List<String> tagIds = new ArrayList<>();
        for (Tag tag : payeeTags) {
            tagIds.add(tag.getId());
        }
        Set<String> personIds = personInfoService.searchPersonInPropTagsByPage(personInfo.getPerson().getId(), tagIds, count, skip);
        for (String unionid : personIds) {
            PersonInfo info = personInfoService.getInfo(unionid);
            if (info == null) {
                continue;
            }
            unrecommendeds.put(info.getPerson().getId(), info);
        }
        return true;
    }

    private boolean doArea(PersonInfo personInfo, Map<String, PersonInfo> unrecommendeds, int limit, long skip) {
        Area area = personInfo.getPayeeArea();
        if (area == null) {
            return false;
        }
        List<PersonInfo> personInfos = personInfoService.searchByArea(personInfo, limit, skip);
        for (PersonInfo info : personInfos) {
            unrecommendeds.put(info.getPerson().getId(), info);
        }
        return true;
    }

    class MyPredicate implements Predicate<PersonInfo> {
        List<String> ids;

        public MyPredicate(List<String> ids) {
            this.ids = ids;
        }

        @Override
        public boolean test(PersonInfo o) {
            return ids.contains(o.getPerson().getId());
        }
    }
}
