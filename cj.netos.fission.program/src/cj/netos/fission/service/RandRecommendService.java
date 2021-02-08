package cj.netos.fission.service;

import cj.netos.fission.IPersonService;
import cj.netos.fission.IRandRecommendService;
import cj.netos.fission.model.Person;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;

@CjService(name = "randRecommendService")
public class RandRecommendService implements IRandRecommendService {
    public static String _KEY = "rand-recommend-persons";
    @CjServiceRef
    IPersonService personService;
    @CjServiceRef(refByName = "@.redis.cluster")
    JedisCluster jedisCluster;

    @Override
    public List<Person> randPersons(int count) throws CircuitException {
        List<String> unionids = jedisCluster.srandmember(_KEY, count);
        return personService.findByIds(unionids);
    }

    @Override
    public int cachePersons(int count) throws CircuitException {
        List<Person> persons = personService.page(count, 0);
        String[] ids = new String[persons.size()];
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            ids[i] = person.getId();
        }
        if (ids.length > 0) {
            jedisCluster.sadd(_KEY, ids);
        }
        return persons.size();
    }

    @Override
    public void cachePerson(Person person) throws CircuitException {
        jedisCluster.sadd(_KEY, person.getId());
    }

    @Override
    public long cachedPersonCount() throws CircuitException {
        return 0;
    }
}
