package cj.netos.fission.service;

import cj.netos.fission.*;
import cj.netos.fission.model.*;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@CjService(name = "personInfoService")
public class PersonInfoService implements IPersonInfoService {
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
        List<Area> areas = areaService.listLimitArea(unionid);
        Area payerArea = null;
        Area payeeArea = null;
        for (Area area : areas) {
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
    public List<PersonInfo> searchByArea(PersonInfo personInfo, int limit, long skip) {
        List<Tag> payeeTags = personInfo.getPayeeTags();
        if (payeeTags.isEmpty()) {
            return searchByAreaNotPayeeTag(personInfo, limit, skip);
        }
        return searchByAreaWithPayeeTag(personInfo, limit, skip);
    }

    private List<PersonInfo> searchByAreaWithPayeeTag(PersonInfo personInfo, int limit, long skip) {
        List<Tag> payeeTags = personInfo.getPayeeTags();
        List<String> tagIds = new ArrayList<>();
        for (Tag tag : payeeTags) {
            tagIds.add(tag.getId());
        }
        List<String> personIds = searchPersonInPropTags(personInfo.getPerson().getId(), tagIds);
        Area area = personInfo.getPayeeArea();
        List<PersonInfo> personInfos = new ArrayList<>();
        switch (area.getAreaType()) {
            case "around":
                personInfos = searchByAroundIn(personInfo.getPerson().getLocation(), area.getAreaCode(), personIds, limit, skip);
                break;
            case "province":
                personInfos = searchByProvinceIn(area.getAreaCode(), personIds, limit, skip);
                break;
            case "city":
                personInfos = searchByCityIn(area.getAreaCode(), personIds, limit, skip);
                break;
        }
        return personInfos;
    }

    @Override
    public Set<String> searchPersonInPropTagsByPage(String personId, List<String> tagIds, int limit, long skip) {
        return tagService.searchPersonInPropTagsByPage(personId, tagIds, limit, skip);
    }

    @Override
    public List<String> searchPersonInPropTags(String personId, List<String> tagIds) {
        return tagService.searchPersonInPropTags(personId, tagIds);
    }

    private List<PersonInfo> searchByAreaNotPayeeTag(PersonInfo personInfo, int limit, long skip) {
        Area area = personInfo.getPayeeArea();
        List<PersonInfo> personInfos = new ArrayList<>();
        switch (area.getAreaType()) {
            case "around":
                personInfos = searchByAround(personInfo.getPerson().getLocation(), area.getAreaCode(), limit, skip);
                break;
            case "province":
                personInfos = searchByProvince(area.getAreaCode(), limit, skip);
                break;
            case "city":
                personInfos = searchByCity(area.getAreaCode(), limit, skip);
                break;
        }
        return personInfos;
    }

    private List<PersonInfo> searchByCity(String cityCodeFull, int limit, long skip) {
        String[] arr = cityCodeFull.split("·");
        String provinceCode = arr[0];
        String cityCode = arr[1];
        List<Person> personList = personService.findInCity(provinceCode, cityCode, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Person person : personList) {
            PersonInfo info = loadPersonInfo(person);
            infos.add(info);
        }
        return infos;
    }

    private List<PersonInfo> searchByCityIn(String cityCodeFull, List<String> personIds, int limit, long skip) {
        String[] arr = cityCodeFull.split("·");
        String provinceCode = arr[0];
        String cityCode = arr[1];
        List<Person> personList = personService.findInCityIn(provinceCode, cityCode, personIds, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Person person : personList) {
            PersonInfo info = loadPersonInfo(person);
            infos.add(info);
        }
        return infos;
    }

    private List<PersonInfo> searchByProvince(String provinceCode, int limit, long skip) {
        List<Person> personList = personService.findInProvince(provinceCode, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Person person : personList) {
            PersonInfo info = loadPersonInfo(person);
            infos.add(info);
        }
        return infos;
    }

    private List<PersonInfo> searchByProvinceIn(String provinceCode, List<String> personIds, int limit, long skip) {
        List<Person> personList = personService.findInProvinceIn(provinceCode, personIds, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Person person : personList) {
            PersonInfo info = loadPersonInfo(person);
            infos.add(info);
        }
        return infos;
    }

    private List<PersonInfo> searchByAround(LatLng location, String radius, int limit, long skip) {
        AggregateIterable<Document> documents = personService.findInAround(location, radius, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> tuple = (Map<String, Object>) document.get("tuple");
            Person person = Person.load(tuple);
            PersonInfo info = loadPersonInfo(person);
            info.setDistance((double) tuple.get("distance"));
            infos.add(info);
        }
        return infos;
    }

    private List<PersonInfo> searchByAroundIn(LatLng location, String radius, List<String> personIds, int limit, long skip) {
        AggregateIterable<Document> documents = personService.findInAroundByIds(location, radius, personIds, limit, skip);
        List<PersonInfo> infos = new ArrayList<>();
        for (Document document : documents) {
            Map<String, Object> tuple = (Map<String, Object>) document.get("tuple");
            Person person = Person.load(tuple);
            PersonInfo info = loadPersonInfo(person);
            info.setDistance((double) tuple.get("distance"));
            infos.add(info);
        }
        return infos;
    }

    @Override
    public Attachment getAttachment(String id) {
        return attachmentService.getInfo(id);
    }
}
