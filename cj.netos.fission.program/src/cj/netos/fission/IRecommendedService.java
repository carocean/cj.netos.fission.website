package cj.netos.fission;

import cj.netos.fission.model.Recommended;

import java.util.List;

public interface IRecommendedService {
    List<Recommended> page(String unionid, int limit, int skip);

    List<String> pageExcludeIds(List<String> idList);

}
