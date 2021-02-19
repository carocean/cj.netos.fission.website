package cj.netos.fission;

import cj.netos.fission.model.Attachment;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;

import java.util.List;
import java.util.Set;

public interface IPersonInfoService {
    PersonInfo getInfo(String unionid);

    PersonInfo loadPersonInfo(Person person);

    List<PersonInfo> searchByArea(PersonInfo personInfo, int limit, long skip);

    List<String> searchPersonInPropTags(String personId, List<String> tagIds);

    Set<String> searchPersonInPropTagsByPage(String id, List<String> tagIds, int limit, long skip);

    long totalPerson();

    Attachment getAttachment(String id);

}
