package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.fission.AbstractService;
import cj.netos.fission.IAreaService;
import cj.netos.fission.IPersonService;
import cj.netos.fission.model.Area;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

import java.util.ArrayList;
import java.util.List;

@CjService(name = "areaService")
public class AreaService extends AbstractService implements IAreaService {
    @Override
    public Area getLimitArea(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.person':'%s'}", _KEY_COL_LIMIT_AREA, Area.class.getName(), unionid);
        IQuery<Area> query = getHome().createQuery(cjql);
        IDocument<Area> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }


}
