package org.kie.kogito.serverless.workflow.executor;

import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.kogito.Application;
import org.kie.kogito.Model;
import org.kie.kogito.correlation.CompositeCorrelation;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.impl.AbstractProcess;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

class StaticWorkflowProcess extends AbstractProcess<JsonNodeModel> {

    private final KogitoWorkflowProcess process;

    public StaticWorkflowProcess(Application app, KogitoWorkflowProcess process) {
        this.app = app;
        this.process = process;
    }

    @Override
    public ProcessInstance<JsonNodeModel> createInstance(JsonNodeModel model) {
        return new StaticWorkflowProcessInstance(this, model, this.createProcessRuntime());
    }

    @Override
    public ProcessInstance<JsonNodeModel> createInstance(String businessKey, CompositeCorrelation correlation,
            JsonNodeModel model) {
        return new StaticWorkflowProcessInstance(this, model, this.createProcessRuntime(), businessKey, correlation);
    }

    @Override
    public ProcessInstance<? extends Model> createInstance(Model m) {
        return createInstance((JsonNodeModel) m);
    }

    @Override
    public ProcessInstance<JsonNodeModel> createInstance(WorkflowProcessInstance wpi) {
        return new StaticWorkflowProcessInstance(this, this.createModel(), this.createProcessRuntime(), wpi);
    }

    @Override
    public ProcessInstance<JsonNodeModel> createReadOnlyInstance(WorkflowProcessInstance wpi) {
        return new StaticWorkflowProcessInstance(this, this.createModel(), wpi);
    }

    @Override
    protected Process process() {
        return process;
    }

}
