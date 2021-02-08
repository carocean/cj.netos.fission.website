package cj.netos.fission;

import cj.netos.fission.model.Person;

import java.util.Map;

public interface ITagRecommendService {
    Map<Person, Boolean> recommend(String unionid, int count);

}
