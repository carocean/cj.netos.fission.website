package cj.netos.fission;

import cj.netos.fission.model.Person;

import java.util.Map;

public interface IPersonGroupService {
    boolean hasNext(String unionid);


    Map<Person, Boolean> nextGroup(String unionid, int count);

    Map<Person, Boolean> current(String unionid);

}
