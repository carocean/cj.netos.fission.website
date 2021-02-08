package cj.netos.fission.service;

import cj.netos.fission.IPersonGroupService;
import cj.netos.fission.ITagRecommendService;
import cj.netos.fission.model.Person;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;

import java.util.Map;

@CjService(name = "tagRecommendService")
public class TagRecommendService implements ITagRecommendService {
    @CjServiceRef
    IPersonGroupService personGroupService;
    @Override
    public Map<Person, Boolean> recommend(String unionid, int count) {
        if(personGroupService.hasNext(unionid)){
            return personGroupService.nextGroup(unionid,count);
        }
        return personGroupService.current(unionid);
    }
}
