package cj.netos.fission;

import cj.netos.fission.model.Person;
import cj.studio.ecm.net.CircuitException;

import java.util.List;

public interface IRandRecommendService {
    List<Person> randPersons(int count) throws CircuitException;

    int cachePersons(int count) throws CircuitException;

    void cachePerson(Person person) throws CircuitException;

    long cachedPersonCount() throws CircuitException;
}
