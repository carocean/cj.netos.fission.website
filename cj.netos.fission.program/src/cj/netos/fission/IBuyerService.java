package cj.netos.fission;

import cj.netos.fission.model.OthersToMeCondition;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PropertyTag;

import java.util.List;

/**
 * 只有那些出纳柜台上有钱的用户才叫购买者，才会被推荐给其它用户，没钱的用户不会被推荐
 */
public interface IBuyerService {
    /**
     * 缓冲出纳柜台有钱的用户
     */
    long cacheHasCashierBalance();
    List<Person> readBuyersByCondition(String unionid, List<OthersToMeCondition> othersToMeConditions, int count);

    List<Person> readBuyersByTag(String unionid, List<PropertyTag> tags, int size);

    List<String> readBuyersByRandom(String unionid, int size);

}
