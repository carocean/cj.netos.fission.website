package cj.netos.fission.service;

import cj.netos.fission.*;
import cj.netos.fission.model.OthersToMeCondition;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PropertyTag;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

import java.util.ArrayList;
import java.util.List;

@CjService(name = "personRecommendBucketService")
public class PersonRecommendBucketService implements IPersonRecommendBucketService {
    @CjServiceRef
    IBuyerService buyerService;
    @CjServiceRef
    IRecommendedService recommendedService;
    @CjServiceRef
    IOtherToMeConditionService otherToMeConditionService;
    @CjServiceRef
    IPropertyTagService propertyTagService;


    @Override
    public List<String> recommend(String unionid, int count) {
        List<String> unionids = new ArrayList<>();
        doRecommend(unionid, count, unionids);
        return unionids;
    }

    //注意：优先按限定条件匹配，如果没有记录则按其属性标签匹配，如果仍无记录则按隐含的“任意“兴趣标签在所有用户中选出
    private void doRecommend(String unionid, int count, List<String> unionids) {
        List<OthersToMeCondition> othersToMeConditions = otherToMeConditionService.listCondition(unionid);
        if (!othersToMeConditions.isEmpty()) {//根据我要求推荐的条件查找用户
            List<Person> personList = buyerService.readBuyersByCondition(unionid,othersToMeConditions, count);
            for (Person person : personList) {
                unionids.add(person.getId());
            }
        }
        if (unionids.size() >= count) {
            return;
        }
        List<PropertyTag> tags = propertyTagService.listTag(unionid);
        if (!tags.isEmpty()) {//根据我本身的兴趣查找用户
            List<Person> personList = buyerService.readBuyersByTag(unionid,tags, count-unionids.size());
            for (Person person : personList) {
                unionids.add(person.getId());
            }
        }
        if (unionids.size() >= count) {
            return;
        }
        //如果扔不够则在用户表中随机查找
        List<String> idList = buyerService.readBuyersByRandom(unionid,count-unionids.size());
        List<String> notInList=recommendedService.pageExcludeIds(idList);
        unionids.addAll(notInList);
    }
}
