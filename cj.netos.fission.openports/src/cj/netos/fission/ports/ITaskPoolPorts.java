package cj.netos.fission.ports;

import cj.netos.fission.model.TaskPool;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.PKeyInRequest;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.List;

@CjOpenports(usage = "任务池配置")
public interface ITaskPoolPorts extends IOpenportService {
    @CjOpenport(usage = "配置任务池，会先清除先前配置",command = "post")
    void config(ISecuritySession securitySession,
                @CjOpenportParameter(usage = "任务数据", name = "tasks", elementType = TaskPool.class,in = PKeyInRequest.content) List<TaskPool> tasks
    ) throws CircuitException;
}
