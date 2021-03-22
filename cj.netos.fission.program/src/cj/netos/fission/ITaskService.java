package cj.netos.fission;

import cj.netos.fission.model.TaskCounter;
import cj.netos.fission.model.TaskEvent;
import cj.netos.fission.model.TaskPool;

import java.util.List;

public interface ITaskService {
    long getOpTimers(String person);


    TaskCounter getCounter(String person);

    void doneTask(String person, String nickName, String eventType, String eventTitle);

    List<TaskEvent> pageEvent(String person, int limit, long skip);

    String genCondition(String unionid);

    void config(List<TaskPool> tasks);

    TaskPool getTask(String task);

}
