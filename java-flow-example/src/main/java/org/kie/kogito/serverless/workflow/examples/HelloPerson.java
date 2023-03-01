package org.kie.kogito.serverless.workflow.examples;

import java.util.Arrays;
import java.util.Collections;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowExecutor;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.filters.StateDataFilter;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.workflow.Functions;

public class HelloPerson {

    private static final String START_STATE = "start";
    private static final String FUNCTION_NAME = "concat";

    public static void main(String[] args) {
        // define your flow using Serverless workflow SDK 
        Workflow workflow = new Workflow("HelloPerson", "Hello Person", "1.0", Arrays.asList(
                new OperationState().withName(START_STATE).withType(Type.OPERATION).withStateDataFilter(new StateDataFilter().withOutput(".response"))
                        .withActions(Arrays.asList(new Action().withFunctionRef(new FunctionRef(FUNCTION_NAME)))).withEnd(new End())))
                                .withStart(new Start().withStateName(START_STATE))
                                .withFunctions(new Functions(Arrays.asList(new FunctionDefinition(FUNCTION_NAME)
                                        .withType(FunctionDefinition.Type.EXPRESSION)
                                        .withOperation("\"My name is \"+.name"))));

        // create a reusable process for several executions
        Process<JsonNodeModel> process = StaticWorkflowApplication.get().process(workflow);
        // execute it with one person name
        System.out.println(StaticWorkflowExecutor.execute(process, Collections.singletonMap("name", "Javierito")));
        // execute it with other person name
        System.out.println(StaticWorkflowExecutor.execute(process, Collections.singletonMap("name", "Mark")));
    }
}
