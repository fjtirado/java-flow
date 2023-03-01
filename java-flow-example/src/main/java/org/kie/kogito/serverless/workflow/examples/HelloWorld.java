package org.kie.kogito.serverless.workflow.examples;

import java.util.Arrays;
import java.util.Collections;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowExecutor;

import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.InjectState;

public class HelloWorld {

    private static final String START_STATE = "start";

    public static void main(String[] args) {
        // define your flow using Serverless workflow SDK 
        Workflow workflow = new Workflow("Hello", "Hello World", "1.0", Arrays.asList(
                new InjectState(START_STATE, Type.INJECT).withData(new TextNode("Hello World!!!")).withEnd(new End())))
                        .withStart(new Start().withStateName(START_STATE));
        // execute it 
        System.out.println(StaticWorkflowExecutor.execute(workflow, Collections.emptyMap()));
    }
}
