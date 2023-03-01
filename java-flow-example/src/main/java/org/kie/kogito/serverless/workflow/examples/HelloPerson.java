package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowExecutor;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition;

import static org.kie.kogito.serverless.workflow.fluent.DataFilterFactory.outputFilter;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.workflow;

public class HelloPerson {

    private static final String START_STATE = "start";
    private static final String FUNCTION_NAME = "name";
    private static final String FUNCTION_SURNAME = "surname";

    public static void main(String[] args) {
        // define your flow using Serverless workflow SDK, sequential function call 
        Workflow workflow = workflow("HelloPerson")
                .function(FUNCTION_NAME, FunctionDefinition.Type.EXPRESSION, "\"My name is \"+.name")
                .function(FUNCTION_SURNAME, FunctionDefinition.Type.EXPRESSION, ".response+\" and my surname is \"+.surname")
                .operation(START_STATE,
                        actionFactory -> actionFactory.functionCall(FUNCTION_NAME).functionCall(FUNCTION_SURNAME),
                        state -> state.withStateDataFilter(outputFilter(".response")))
                .build();

        // create a reusable process for several executions
        Process<JsonNodeModel> process = StaticWorkflowApplication.get().process(workflow);
        // execute it with one person name
        System.out.println(StaticWorkflowExecutor.execute(process, Map.of("name", "Javier", "surname", "Tirado")));
        // execute it with other person name
        System.out.println(StaticWorkflowExecutor.execute(process, Map.of("name", "Mark", "surname", "Proctor")));
    }
}
