package org.kie.kogito.serverless.workflow.examples;

import java.util.Collections;

import org.jboss.logging.Logger;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;

import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.inject;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class HelloWorld {

	private static final Logger logger = Logger.getLogger(HelloWorld.class); 
	
    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            // define your flow using Fluent version Serverless workflow SDK
            Workflow workflow = workflow("HelloWorld").singleton(inject(new TextNode("Hello World!!!")));
            /*
             * this is equivalent to raw version of Serverless workflow sdk
             * new Workflow("HelloWorld", "Hello World", "1.0", Arrays.asList(
             * new InjectState(START_STATE, Type.INJECT).withData(new TextNode("Hello World!!!")).withEnd(new End())))
             * .withStart(new Start().withStateName(START_STATE));
             */
            // execute it 
            logger.info(application.execute(workflow, Collections.emptyMap()).getWorkflowdata().toPrettyString());
        }
    }
}
