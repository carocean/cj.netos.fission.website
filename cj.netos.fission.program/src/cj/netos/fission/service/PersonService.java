package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.Person;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.ultimate.gson2.com.google.gson.Gson;
import com.mongodb.client.ListIndexesIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@CjService(name = "personService")
public class PersonService extends AbstractService implements IPersonService {
    @Override
    public void createGeoIndex() {
        //为感知器和文档创建地理索引
        ListIndexesIterable<Document> itReceptorIndexes = getHome().listIndexes(_KEY_COL);
        boolean hasIndex = false;
        for (Document doc : itReceptorIndexes) {
            //是否存在索引，如果不存在则建立
            Map map = (Map) doc.get("key");
            Object v = map.get("tuple.location");
//                System.out.println(doc);
            if (v != null) {
//                    home.dropIndex(_getReceptorColName(category.getId()), Document.parse(String.format("{'tuple.location':1}")));
                CJSystem.logging().info(getClass(), String.format("发现用户表地理<%s>的索引：tuple.location=2dsphere", _KEY_COL));
                hasIndex = true;
                break;
            }
        }
        if (!hasIndex) {
            getHome().createIndex(_KEY_COL, Document.parse(String.format("{'tuple.location':'2dsphere'}")));
            CJSystem.logging().info(getClass(), String.format("已为用户表<%s>创建地理索引：tuple.location=2dsphere", _KEY_COL));
        }

    }

    @Override
    public void add(Person person) throws CircuitException {
        getHome().saveDoc(_KEY_COL, new TupleDocument<>(person));
    }

    @Override
    public boolean exists(String id) {
        return getHome().tupleCount(_KEY_COL, String.format("{'tuple.id':'%s'}", id)) > 0;
    }

    @Override
    public Person get(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.id':'%s'}", _KEY_COL, Person.class.getName(), unionid);
        IQuery<Person> query = getHome().createQuery(cjql);
        IDocument<Person> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }

    @Override
    public List<Person> page(int limit, int skip) {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s).sort({'tuple.ctime':-1}) from tuple %s %s where {}", limit, skip, _KEY_COL, Person.class.getName());
        IQuery<Person> query = getHome().createQuery(cjql);
        List<IDocument<Person>> documents = query.getResultList();
        List<Person> persons = new ArrayList<>();
        for (IDocument<Person> document : documents) {
            persons.add(document.tuple());
        }
        return persons;
    }

    @Override
    public List<Person> findByIds(List<String> unionids) {
        String cjql = String.format("select {'tuple':'*'}.sort({'tuple.ctime':-1}) from tuple %s %s where {'tuple.id':{'$in':%s}}", _KEY_COL, Person.class.getName(), new Gson().toJson(unionids));
        IQuery<Person> query = getHome().createQuery(cjql);
        List<IDocument<Person>> documents = query.getResultList();
        List<Person> persons = new ArrayList<>();
        for (IDocument<Person> document : documents) {
            persons.add(document.tuple());
        }
        return persons;
    }
}
