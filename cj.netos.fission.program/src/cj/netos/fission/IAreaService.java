package cj.netos.fission;

import cj.netos.fission.model.Area;
import cj.netos.fission.model.PersonInfo;

import java.util.List;

public interface IAreaService {
    String _KEY_COL_LIMIT_AREA="fission.mf.limit.areas"   ;
    Area getLimitArea(String unionid);

}
