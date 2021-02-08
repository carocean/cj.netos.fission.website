package cj.netos.fission;

import cj.netos.fission.model.Person;
import cj.studio.ecm.net.CircuitException;

import java.util.List;

public interface IPersonService {
    public  static  String _KEY_COL="fission.mf.persons";

    void createGeoIndex();

    void add(Person person)throws CircuitException;
    boolean exists(String id);

    Person get(String unionid);

    List<Person> page(int limit, int skip);

    List<Person> findByIds(List<String> unionids);

}
