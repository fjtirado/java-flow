package org.kie;

import java.util.Map;

import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;

import io.serverlessworkflow.api.Workflow;

public class StaticWorkflowExecutor {

    public static Map<String, Object> execute(Workflow workflow, Map<String, Object> data) {
        StaticWorkflowProcess process = new StaticWorkflowProcess(ServerlessWorkflowParser
                .of(workflow, JavaKogitoBuildContext.builder().build()).getProcessInfo().info());
        ProcessInstance<JsonNodeModel> processInstance = process.createInstance(new JsonNodeModel(data));
        processInstance.start();
        return processInstance.variables().toMap();
    }
}
