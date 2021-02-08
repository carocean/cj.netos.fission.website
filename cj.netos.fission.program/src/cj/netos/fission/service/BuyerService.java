package cj.netos.fission.service;

import cj.netos.fission.ICashierBalanceService;
import cj.netos.fission.IBuyerService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.OthersToMeCondition;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PropertyTag;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import redis.clients.jedis.JedisCluster;

import java.util.List;

@CjService(name = "buyerService")
public class BuyerService implements IBuyerService {
    static final String _KEY = "fission.mf.sales.person";
    @CjServiceRef
    ICashierBalanceService cashierBalanceService;
    @CjServiceRef(refByName = "@.redis.cluster")
    JedisCluster jedisCluster;
    @CjServiceRef
    IPersonService personService;

    @Override
    public long cacheHasCashierBalance() {
        int limit = 500;
        int skip = 0;
        while (true) {
            List<String> unionids = cashierBalanceService.pagePersonByCashierOpening(limit, skip);
            if (unionids.isEmpty()) {
                break;
            }
            String[] arr = new String[unionids.size()];
            for (int i = 0; i < unionids.size(); i++) {
                String id = unionids.get(i);
                arr[i] = id;
            }
            //缓冲那些采购用户的人，只有他们才会被推荐给其他用户。那些不充钱的用户是不会被推荐的
            jedisCluster.sadd(_KEY, arr);
            skip += unionids.size();
        }
        return skip;
    }

    //尽量按count取满
    @Override
    public List<Person> readBuyersByCondition(String unionid, List<OthersToMeCondition> othersToMeConditions, int count) {
        //先在采购者缓冲中随机出来指定数量*2的用户，然后再判断是否符合推荐条件，不附合则再从缓冲中随机取指定数量*2
        return null;
    }

    //尽量按count取满
    @Override
    public List<Person> readBuyersByTag(String unionid, List<PropertyTag> tags, int count) {
        //先在采购者缓冲中随机出来指定数量*2的用户，然后再判断是否符合推荐条件，不附合则再从缓冲中随机取指定数量*2
        return null;
    }

    //尽量按count取满
    @Override
    public List<String> readBuyersByRandom(String unionid, int count) {
        //先在采购者缓冲中随机出来指定数量*2的用户
        List<String> ids = jedisCluster.srandmember(_KEY, count);
        return ids;
    }
}
