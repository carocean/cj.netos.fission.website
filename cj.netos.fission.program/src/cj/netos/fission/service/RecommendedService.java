package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IRecommendedService;
import cj.netos.fission.model.Recommended;
import cj.studio.ecm.annotation.CjService;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@CjService(name = "recommendedService")
public class RecommendedService extends AbstractService implements IRecommendedService {
    static String _KEY_COL = "fission.mf.recommended";

    @Override
    public List<Recommended> page(String unionid, int limit, int skip) {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s) from tuple %s %s where {'tuple.recommender':'%s'}", limit, skip, _KEY_COL, Recommended.class.getName(), unionid);
        IQuery<Recommended> query = getHome().createQuery(cjql);
        List<IDocument<Recommended>> documents = query.getResultList();
        List<Recommended> recommendeds = new ArrayList<>();
        for (IDocument<Recommended> document : documents) {
            recommendeds.add(document.tuple());
        }
        return recommendeds;
    }

    @Override
    public List<String> listExcludeIds(List<String> idList) {
        String cjql = String.format("select {'tuple.person':1} from tuple %s %s where {'tuple.person':{'$in':%s}}", _KEY_COL, String.class.getName(), new Gson().toJson(idList));
        IQuery<String> query = getHome().createQuery(cjql);
        List<IDocument<String>> documents = query.getResultList();
        for (IDocument<String> document : documents) {
            idList.removeIf(new MyPredicate(document));
        }
        return idList;
    }

    @Override
    public List<String> listIncludeIds(List<String> idList) {
        String cjql = String.format("select {'tuple.person':1} from tuple %s %s where {'tuple.person':{'$in':%s}}", _KEY_COL, String.class.getName(), new Gson().toJson(idList));
        IQuery<String> query = getHome().createQuery(cjql);
        List<IDocument<String>> documents = query.getResultList();
        List<String> exists = new ArrayList<>();
        for (IDocument<String> document : documents) {
            exists.add(document.tuple());
        }
        return exists;
    }

    class MyPredicate implements Predicate<String> {
        IDocument<String> document;

        public MyPredicate(IDocument<String> document) {
            this.document = document;
        }

        @Override
        public boolean test(String o) {
            return document.tuple().equals(o);
        }
    }
}
