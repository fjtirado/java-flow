package org.kie.kogito.serverless.workflow.executor;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;

public class StaticWorkflowExecutor {

    public static JsonNodeModel execute(Process<JsonNodeModel> process, Map<String, Object> data) {
        ProcessInstance<JsonNodeModel> processInstance = process.createInstance(new JsonNodeModel(data));
        processInstance.start();
        return processInstance.variables();
    }

    public static Map<String, Object> execute(Workflow workflow, Map<String, Object> data) {
        return execute(StaticWorkflowApplication.get().process(workflow), data).toMap();
    }
}
