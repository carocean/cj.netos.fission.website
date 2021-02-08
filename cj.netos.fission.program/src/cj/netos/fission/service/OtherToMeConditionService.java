package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IOtherToMeConditionService;
import cj.netos.fission.model.OthersToMeCondition;
import cj.studio.ecm.annotation.CjService;

import java.util.ArrayList;
import java.util.List;
@CjService(name = "otherToMeConditionService")
public class OtherToMeConditionService extends AbstractService implements IOtherToMeConditionService {
    @Override
    public List<OthersToMeCondition> listCondition(String unionid) {
        String cjql = String.format("select {'tuple':'*'}.sort({'tuple.type':1}) from tuple %s %s where {'tuple.person':'%s'}",  _KEY_COL, OthersToMeCondition.class.getName(),unionid);
        IQuery<OthersToMeCondition> query = getHome().createQuery(cjql);
        List<IDocument<OthersToMeCondition>> documents = query.getResultList();
        List<OthersToMeCondition> persons = new ArrayList<>();
        for (IDocument<OthersToMeCondition> document : documents) {
            persons.add(document.tuple());
        }
        return persons;
    }
}
