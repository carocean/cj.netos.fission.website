package cj.netos.fission;

import java.util.List;

public interface IRecommendedService {
    void visitIdList(String person,List<String> unionIds);

    void emptyVisits(String unionid);
}
