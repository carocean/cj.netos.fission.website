package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IPersonGroupService;
import cj.netos.fission.IPersonRecommendBucketService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.PersonGroup;
import cj.netos.fission.model.Person;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

import java.text.SimpleDateFormat;
import java.util.*;

@CjService(name = "personGroupService")
public class PersonGroupService extends AbstractService implements IPersonGroupService {
    public static String _KEY_COL = "fission.mf.groups";
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef
    IPersonRecommendBucketService personRecommendBucketService;

    @Override
    public boolean hasNext(String unionid) {
        PersonGroup personGroup = get(unionid);
        if (personGroup == null) {
            return true;
        }
        return personGroup.isDone();
    }


    @Override
    public Map<Person, Boolean> nextGroup(String unionid, int count) {
        PersonGroup personGroup = next(unionid, count);
        Map<String, Boolean> workItems = personGroup.getWorkItems();
        return convertMap(workItems);
    }


    @Override
    public Map<Person, Boolean> current(String unionid) {
        PersonGroup personGroup = get(unionid);
        return convertMap(personGroup.getWorkItems());
    }

    private Map<Person, Boolean> convertMap(Map<String, Boolean> workItems) {
        List<String> ids = new ArrayList<>();
        for (String id : workItems.keySet()) {
            ids.add(id);
        }
        List<Person> personList = personService.findByIds(ids);
        Map<Person, Boolean> items = new HashMap<>();
        for (Person person : personList) {
            items.put(person, workItems.get(person.getId()));
        }
        return items;
    }

    private PersonGroup get(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.groupOwner':'%s'}", _KEY_COL, PersonGroup.class.getName(), unionid);
        IQuery<PersonGroup> query = getHome().createQuery(cjql);
        IDocument<PersonGroup> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }

    private boolean exists(String unionid) {
        return getHome().tupleCount(_KEY_COL, String.format("{'tuple.groupOwner':'%s'}", unionid)) > 0;
    }

    private PersonGroup next(String unionid, int count) {
        //推荐一组
        List<String> unionids = personRecommendBucketService.recommend(unionid, count);
        PersonGroup group = new PersonGroup();
        group.setCapacity(count);
        group.setDone(false);
        group.setGroupOwner(unionid);
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HHmmss");
        String ctime = format.format(new Date(System.currentTimeMillis()));
        group.setCtime(ctime);
        Map<String, Boolean> workItems = group.getWorkItems();
        for (String id : unionids) {
            workItems.put(id, false);
        }
        update(group);
        return group;
    }

    private void update(PersonGroup group) {
        getHome().deleteDocOne(_KEY_COL, String.format("{'tuple.groupOwner':'%s'}", group.getGroupOwner()));
        getHome().saveDoc(_KEY_COL, new TupleDocument<>(group));
    }
}
