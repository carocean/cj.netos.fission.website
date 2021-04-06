package cj.netos.fission.service;

import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.lns.chip.sos.cube.framework.TupleDocument;
import cj.netos.fission.AbstractService;
import cj.netos.fission.ITaskService;
import cj.netos.fission.model.TaskCounter;
import cj.netos.fission.model.TaskEvent;
import cj.netos.fission.model.TaskPool;
import cj.netos.fission.model.Utils;
import cj.studio.ecm.annotation.CjService;
import cj.studio.openport.util.Encript;
import org.bson.Document;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CjService(name = "taskService")
public class TaskService extends AbstractService implements ITaskService {
    static final String _COL_TASK_COUNTER = "fission.mf.task.counters";
    static final String _COL_TASK_EVENT = "fission.mf.task.events";
    static final String _COL_TASK_POOL = "fission.mf.task.pool";

    @Override
    public void config(List<TaskPool> tasks) {
        getHome().dropTuple(_COL_TASK_POOL);
        for (TaskPool task : tasks) {
            getHome().saveDoc(_COL_TASK_POOL, new TupleDocument<>(task));
        }
    }

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
            String update = String.format("{'$set':{'tuple.opTimes':0},'$unset':{'tuple.condition':'','tuple.task':''}}");
            getHome().updateDocOne(_COL_TASK_COUNTER, Document.parse(filter), Document.parse(update));
            return;
        }
        TaskCounter counter = new TaskCounter();
        counter.setOpTimes(0);
        counter.setPerson(person);
        counter.setCondition(null);
        counter.setTask(null);
        getHome().saveDoc(_COL_TASK_COUNTER, new TupleDocument<>(counter));
    }

    @Override
    public void doneTask(String person, String nickName, String eventType, String eventTitle) {
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
    public List<TaskEvent> pageEvent(String person, int limit, long skip) {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s).sort({'tuple.ctime':-1}) from tuple %s %s where {'tuple.person':'%s'}", limit, skip, _COL_TASK_EVENT, TaskEvent.class.getName(), person);
        IQuery<TaskEvent> query = getHome().createQuery(cjql);
        List<IDocument<TaskEvent>> documents = query.getResultList();
        List<TaskEvent> events = new ArrayList<>();
        for (IDocument<TaskEvent> doc : documents) {
            events.add(doc.tuple());
        }
        return events;
    }

    @Override
    public TaskPool getTask(String task) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.task':'%s'}", _COL_TASK_POOL, TaskPool.class.getName(), task);
        IQuery<TaskPool> query = getHome().createQuery(cjql);
        IDocument<TaskPool> document = query.getSingleResult();
        if (document == null) {
            return null;
        }
        return document.tuple();
    }

    @Override
    public String genCondition(String unionid) {
        TaskPool task = selectTask(unionid);
        String condition = Encript.md5(UUID.randomUUID().toString());
        String filter = String.format("{'tuple.person':'%s'}", unionid);
        String update = String.format("{'$set':{'tuple.condition':'%s','tuple.task':'%s'}}", condition, task.getTask());
        getHome().updateDocOne(_COL_TASK_COUNTER, Document.parse(filter), Document.parse(update));
        return condition;
    }

    private TaskPool selectTask(String unionid) {
        String cjql = String.format("select {'tuple':'*'} from tuple %s %s where {'tuple.state':1}", _COL_TASK_POOL, TaskPool.class.getName());
        IQuery<TaskPool> query = getHome().createQuery(cjql);
        List<IDocument<TaskPool>> tasks = query.getResultList();
        //每分享到群、朋友圈各1次之后(查最近执行过的2个任务是否为分享任务)则做一次其它任务，在做其它任务时让用户选择
        List<TaskPool> mustTasks = new ArrayList<>();
        List<TaskPool> otherTasks = new ArrayList<>();
        for (IDocument<TaskPool> taskDoc : tasks) {
            TaskPool task = taskDoc.tuple();
            if (task.isMustDo()) {
                mustTasks.add(task);
            } else {
                otherTasks.add(task);
            }
        }
        List<TaskEvent> events = pageEvent(unionid, 2, 0);
        if (events.isEmpty()) {
            //从must中随机选任务
            int hs = Utils.hash(String.format("%s%s", unionid, System.currentTimeMillis())).intValue();
            hs = Math.abs(hs);
            int index = hs % mustTasks.size();
            return mustTasks.get(index);
        }
        //看哪个must任务上2次没做过
        List<TaskPool> notDoInMust = new ArrayList<>();
        for (TaskPool task : mustTasks) {
            boolean doIt = false;
            for (TaskEvent event : events) {
                if (task.getTask().equals(event.getEventType())) {
                    doIt = true;
                    break;
                }
            }
            if (!doIt) {
                notDoInMust.add(task);
            }
        }
        if (!notDoInMust.isEmpty()) {//从没有做过的必选任务中任选一个
            int hs = Utils.hash(String.format("%s%s", unionid,  System.currentTimeMillis())).intValue();
            hs = Math.abs(hs);
            int index = hs % notDoInMust.size();
            return notDoInMust.get(index);
        }
        //剩下的从其它任务中选一个
        int hs = Utils.hash(String.format("%s%s", unionid, System.currentTimeMillis())).intValue();
        hs = Math.abs(hs);
        int index = hs % otherTasks.size();
        return otherTasks.get(index);
    }
}
