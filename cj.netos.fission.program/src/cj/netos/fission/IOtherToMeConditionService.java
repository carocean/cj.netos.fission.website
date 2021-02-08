package cj.netos.fission;

import cj.netos.fission.model.OthersToMeCondition;

import java.util.List;

public interface IOtherToMeConditionService {
    public  static  String _KEY_COL="fission.mf.condition.o_to_me";
    List<OthersToMeCondition> listCondition(String unionid);

}
