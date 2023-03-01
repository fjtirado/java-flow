package org.kie.kogito.serverless.workflow.executor;

import java.util.Map;

import org.jbpm.process.instance.InternalProcessRuntime;
import org.kie.api.runtime.process.ProcessRuntime;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

class StaticWorkflowProcessInstance extends AbstractProcessInstance<JsonNodeModel> {

    public StaticWorkflowProcessInstance(AbstractProcess<JsonNodeModel> process, JsonNodeModel model, ProcessRuntime rt) {
        super(process, model, rt);
    }

    public StaticWorkflowProcessInstance(StaticWorkflowProcess process, JsonNodeModel model,
            InternalProcessRuntime rt, String businessKey, CompositeCorrelation correlation) {
        super(process, model, businessKey, rt, correlation);
    }

    public StaticWorkflowProcessInstance(StaticWorkflowProcess process, JsonNodeModel model,
            InternalProcessRuntime rt, WorkflowProcessInstance wpi) {
        super(process, model, rt, wpi);
    }

    public StaticWorkflowProcessInstance(StaticWorkflowProcess process, JsonNodeModel model,
            WorkflowProcessInstance wpi) {
        super(process, model, wpi);
    }

    protected Map<String, Object> bind(JsonNodeModel variables) {
        return variables.toMap();
    }

    protected void unbind(JsonNodeModel variables, Map<String, Object> vmap) {
        variables.fromMap(this.id(), vmap);
    }
}
