package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IPropertyTagService;
import cj.netos.fission.model.OthersToMeCondition;
import cj.netos.fission.model.PropertyTag;
import cj.studio.ecm.annotation.CjService;

import java.util.ArrayList;
import java.util.List;
@CjService(name = "propertyTagService")
public class PropertyTagService extends AbstractService implements IPropertyTagService {
    @Override
    public List<PropertyTag> listTag(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.person':'%s'}",  _KEY_COL, PropertyTag.class.getName(),unionid);
        IQuery<PropertyTag> query = getHome().createQuery(cjql);
        List<IDocument<PropertyTag>> documents = query.getResultList();
        List<PropertyTag> persons = new ArrayList<>();
        for (IDocument<PropertyTag> document : documents) {
            persons.add(document.tuple());
        }
        return persons;
    }
}
