package cj.netos.fission;

import cj.netos.fission.model.TaskCounter;
import cj.netos.fission.model.TaskEvent;

public interface ITaskService {
    long getOpTimers(String person);


    TaskCounter getCounter(String person);

    void doEvent(String person,String nickName,String eventType,String eventTitle);

    String genCondition(String unionid);

}
