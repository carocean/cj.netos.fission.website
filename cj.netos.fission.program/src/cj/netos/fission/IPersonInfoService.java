package cj.netos.fission;

import cj.netos.fission.model.Attachment;
import cj.netos.fission.model.Person;
import cj.netos.fission.model.PersonInfo;

import java.util.List;

public interface IPersonInfoService {
    PersonInfo getInfo(String unionid);

    PersonInfo loadPersonInfo(Person person);

    List<PersonInfo> searchByArea(PersonInfo personInfo, int limit, long skip);

    long totalPerson();

    Attachment getAttachment(String id);


    List<PersonInfo> searchByAreaAndPayeeTags(PersonInfo current, int limit, long skip);

    List<PersonInfo> searchByPayeeTags(PersonInfo current, int limit, long skip);

    List<PersonInfo> searchByDefault(PersonInfo current, int limit, long skip);

}
