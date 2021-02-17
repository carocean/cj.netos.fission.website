package cj.netos.fission;

import cj.netos.fission.model.LatLng;
import cj.netos.fission.model.Person;
import cj.studio.ecm.net.CircuitException;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;

import java.util.List;

public interface IPersonService {
    public  static  String _KEY_COL="fission.mf.persons";

    void createGeoIndex();

    void add(Person person)throws CircuitException;
    boolean exists(String id);

    Person get(String unionid);

    List<Person> page(int limit, int skip);

    List<Person> findByIds(List<String> unionids);

    List<Person> findInCity(String provinceCode, String cityCode,int limit,long skip);

    List<Person> findInProvince(String provinceCode,int limit,long skip);

    AggregateIterable<Document> findInAround(LatLng location, String radius, int limit, long skip);

    AggregateIterable<Document> findInAroundByIds(LatLng location, String radiusText, List<String> ids, int limit, long skip);

    List<Person> findInCityIn(String provinceCode, String cityCode, List<String> personIds, int limit, long skip);

    List<Person> findInProvinceIn(String provinceCode, List<String> personIds, int limit, long skip);

    long total();
}
