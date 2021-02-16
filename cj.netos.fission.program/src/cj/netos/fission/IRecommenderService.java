package cj.netos.fission;

import cj.netos.fission.model.PersonInfo;
import cj.studio.ecm.net.CircuitException;

import java.util.Collection;

public interface IRecommenderService {
    Collection<PersonInfo> recommend(String unionid, int count) throws CircuitException;

}
