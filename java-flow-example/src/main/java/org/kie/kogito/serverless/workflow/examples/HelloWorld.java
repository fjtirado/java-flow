package org.kie.kogito.serverless.workflow.examples;

import java.util.Collections;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowExecutor;

import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.workflow;

public class HelloWorld {

    private static final String START_STATE = "start";

    public static void main(String[] args) {
        // define your flow using Fluent version Serverless workflow SDK 
        Workflow workflow = workflow("HelloWorld").inject(START_STATE, new TextNode("Hello World!!!")).build();
        /*
         * this is equivalent to raw version of Serverless workflow sdk
         * new Workflow("HelloWorld", "Hello World", "1.0", Arrays.asList(
         * new InjectState(START_STATE, Type.INJECT).withData(new TextNode("Hello World!!!")).withEnd(new End())))
         * .withStart(new Start().withStateName(START_STATE));
         */
        // execute it 
        System.out.println(StaticWorkflowExecutor.execute(workflow, Collections.emptyMap()));
    }
}
