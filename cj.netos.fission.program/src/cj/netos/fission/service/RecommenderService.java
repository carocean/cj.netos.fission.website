package cj.netos.fission.service;

import cj.netos.fission.*;
import cj.netos.fission.model.*;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;

import java.util.*;

@CjService(name = "recommenderService")
public class RecommenderService implements IRecommenderService {
    @CjServiceRef
    IPersonInfoService personInfoService;
    @CjServiceRef
    IRecommendedService recommendedService;

    @Override
    public Collection<PersonInfo> recommend(String unionid, int count) throws CircuitException {
        PersonInfo current = personInfoService.getInfo(unionid);
        Map<String, PersonInfo> recommendeds = new HashMap<>();
        long totalPerson = personInfoService.totalPerson();
        if (totalPerson == 0) {
            return recommendeds.values();
        }
//        recommendedService.emptyVisits(unionid);//为调试而清除,生产环境必须注释掉

        int limit = count;
        long skip = 0;
        CJSystem.logging().info(getClass(), String.format("开始为用户:%s 推荐，limit=%s", current.getPerson().getNickName(), limit));
        while (skip < totalPerson) {
            if (recommendeds.size() >= count) {
                break;
            }
            doRecommend(current, recommendeds, limit, skip);
            CJSystem.logging().info(getClass(), String.format("\t\t遍历，推荐数:%s",  recommendeds.size()));
            skip += limit;//不论找到多少个推荐，均往下跳过limit个
        }
        CJSystem.logging().info(getClass(), String.format("完成向用户:%s 推荐，实际推荐数=%s", current.getPerson().getNickName(), recommendeds.size()));
        return recommendeds.values();

    }

    private void doRecommend(PersonInfo current, Map<String, PersonInfo> recommendeds, int limit, long skip) {
        Area area = current.getPayeeArea();
        List<Tag> limitTags = current.getPayeeTags();
        if (area != null && !limitTags.isEmpty()) {//两条件都有则求二者的交集
            matchAreaAndPayeeTags(current, recommendeds, limit, skip);
            return;
        }
        if (area != null) {//只有区域条件则只按区域查
            matchArea(current, recommendeds, limit, skip);
            return;
        }

        if (!limitTags.isEmpty()) {//只有标签条件则只按标签查
            matchPayeeTags(current, recommendeds, limit, skip);
            return;
        }
        //没有任何条件则：
        /*
        - 先搜周边
        - 再按属性标签找共同爱好的
        - 再到常规中找
        求三者的并集
         */
        matchDefault(current, recommendeds, limit, skip);
    }

    private void matchDefault(PersonInfo current, Map<String, PersonInfo> recommendeds, int limit, long skip) {
        List<PersonInfo> personInfos = personInfoService.searchByDefault(current, limit, skip);
        for (PersonInfo info : personInfos) {
            recommendeds.put(info.getPerson().getId(), info);
        }
    }

    private void matchAreaAndPayeeTags(PersonInfo current, Map<String, PersonInfo> recommendeds, int limit, long skip) {
        //先查区域得ids，再去除已推荐过的，而后以ids再按被支付人条件查询
        List<PersonInfo> personInfos = personInfoService.searchByAreaAndPayeeTags(current, limit, skip);
        for (PersonInfo info : personInfos) {
            recommendeds.put(info.getPerson().getId(), info);
        }
    }

    private void matchArea(PersonInfo current, Map<String, PersonInfo> recommendeds, int limit, long skip) {
        List<PersonInfo> personInfos = personInfoService.searchByArea(current, limit, skip);
        for (PersonInfo info : personInfos) {
            recommendeds.put(info.getPerson().getId(), info);
        }
    }

    private void matchPayeeTags(PersonInfo current, Map<String, PersonInfo> recommendeds, int limit, long skip) {
        List<PersonInfo> personInfos = personInfoService.searchByPayeeTags(current, limit, skip);
        for (PersonInfo info : personInfos) {
            recommendeds.put(info.getPerson().getId(), info);
        }
    }


}
