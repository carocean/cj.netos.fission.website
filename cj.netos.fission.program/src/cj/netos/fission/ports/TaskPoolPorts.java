package cj.netos.fission.ports;

import cj.netos.fission.ITaskService;
import cj.netos.fission.model.TaskPool;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.annotation.CjServiceRef;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

import java.util.List;

@CjService(name = "/task/pool.ports")
public class TaskPoolPorts implements ITaskPoolPorts {
    @CjServiceRef
    ITaskService taskService;
    @Override
    public void config(ISecuritySession securitySession, List<TaskPool> tasks) throws CircuitException {
        if (!securitySession.roleIn("platform:administrators")) {
            throw  new CircuitException("800","拒绝访问");
        }
        taskService.config(tasks);
    }
}
