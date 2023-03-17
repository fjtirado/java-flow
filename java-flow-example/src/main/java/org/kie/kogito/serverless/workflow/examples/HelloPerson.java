package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.def;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class HelloPerson {

    private static final String FUNCTION_NAME = "name";
    private static final String FUNCTION_SURNAME = "surname";

    public static void main(String[] args) {

        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            // define your flow using Serverless workflow SDK, sequential function call 

            Workflow workflow = workflow("HelloPerson")
                    .function(def(FUNCTION_NAME, FunctionDefinition.Type.EXPRESSION, "\"My name is \"+.name"))
                    .function(def(FUNCTION_SURNAME, FunctionDefinition.Type.EXPRESSION, ".response+\" and my surname is \"+.surname"))
                    .singleton(operation().outputFilter(".response").action(call(FUNCTION_NAME)).action(call(FUNCTION_SURNAME)));

            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(workflow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier", "surname", "Tirado")));
            // execute it with other person name
            System.out.println(application.execute(process, Map.of("name", "Mark", "surname", "Proctor")));
        }
    }
}
