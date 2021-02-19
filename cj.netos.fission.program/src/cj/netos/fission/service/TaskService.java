package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.fission.AbstractService;
import cj.netos.fission.ITaskService;
import cj.netos.fission.model.TaskCounter;
import cj.netos.fission.model.TaskEvent;
import cj.netos.fission.model.Utils;
import cj.studio.ecm.annotation.CjService;
import cj.studio.openport.util.Encript;
import org.bson.Document;

import java.util.UUID;

@CjService(name = "taskService")
public class TaskService extends AbstractService implements ITaskService {
    static final String _COL_TASK_COUNTER = "fission.mf.task.counters";
    static final String _COL_TASK_EVENT = "fission.mf.task.events";

    @Override
    public long getOpTimers(String person) {
        TaskCounter counter = getCounter(person);
        if (counter == null) {
            return 0;
        }
        return counter.getOpTimes();
    }

    @Override
    public TaskCounter getCounter(String person) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.person':'%s'}", _COL_TASK_COUNTER, TaskCounter.class.getName(), person);
        IQuery<TaskCounter> query = getHome().createQuery(cjql);
        IDocument<TaskCounter> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }

    private void clearOptions(String person) {
        String filter = String.format("{'tuple.person':'%s'}", person);
        if (getHome().tupleCount(_COL_TASK_COUNTER, filter) > 0) {
            String update = String.format("{'$set':{'tuple.opTimes':0},'$unset':{'tuple.condition':''}}");
            getHome().updateDocOne(_COL_TASK_COUNTER, Document.parse(filter), Document.parse(update));
            return;
        }
        TaskCounter counter = new TaskCounter();
        counter.setOpTimes(0);
        counter.setPerson(person);
        counter.setCondition(null);
        getHome().saveDoc(_COL_TASK_COUNTER, new TupleDocument<>(counter));
    }

    @Override
    public void doEvent(String person, String nickName, String eventType, String eventTitle) {
        TaskEvent event = new TaskEvent();
        long opTimers = getOpTimers(event.getPerson());
        event.setId(Encript.md5(UUID.randomUUID().toString()));
        event.setPerson(person);
        event.setNickName(nickName);
        event.setEventType(eventType);
        event.setEventTitle(eventTitle);
        event.setOpTimes(opTimers);
        event.setCtime(Utils.timeToStr(System.currentTimeMillis()));
        getHome().saveDoc(_COL_TASK_EVENT, new TupleDocument<>(event));
        //做了任务则清除计数
        clearOptions(event.getPerson());
    }

    @Override
    public String genCondition(String unionid) {
        String condition = Encript.md5(UUID.randomUUID().toString());
        String filter = String.format("{'tuple.person':'%s'}", unionid);
        String update = String.format("{'$set':{'tuple.condition':'%s'}}", condition);
        getHome().updateDocOne(_COL_TASK_COUNTER, Document.parse(filter), Document.parse(update));
        return condition;
    }
}
