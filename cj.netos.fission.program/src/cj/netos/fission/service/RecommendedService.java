package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IRecommendedService;
import cj.studio.ecm.annotation.CjService;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.List;

@CjService(name = "recommendedService")
public class RecommendedService extends AbstractService implements IRecommendedService {
    static final String _COL = "fission.mf.recommendeds";

    @Override
    public void emptyVisits(String unionid) {
        getHome().deleteDocs(_COL, String.format("{'tuple.person':'%s'}", unionid));
    }

    //访问用户标识列表
    //- 如果没访问过则更新为访问过
    //- 返回集合参数中没有访问过的
    @Override
    public void visitIdList(String person, List<String> unionIds) {
        String cjql = String.format("select {'tuple.friend':1}.distinct()  from tuple %s %s where {'tuple.person':'%s','tuple.friend':{'$in':%s}}", _COL, String.class.getName(), person, new Gson().toJson(unionIds));
        IQuery<String> query = getHome().createQuery(cjql);
        List<IDocument<String>> documents = query.getResultList();
        for (IDocument<String> document : documents) {
            String friend = document.tuple();
            unionIds.remove(friend);
        }
    }
}
