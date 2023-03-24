package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class HelloPerson {

    private static final String FUNCTION_NAME = "name";
    private static final String JAVA_FUNCTION = "printResponse";
    private static final String FUNCTION_SURNAME = "surname";

    private static final Logger logger = LoggerFactory.getLogger(HelloPerson.class);

    public static void main(String[] args) {

        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            // sequential function call in two states
            Workflow workflow = workflow("HelloPerson")
                    .function(expr(FUNCTION_NAME, "\"My name is \"+.name"))
                    .function(expr(FUNCTION_SURNAME, ".response+\" and my surname is \"+.surname"))
                    .start(operation().outputFilter(".response").action(call(FUNCTION_NAME)).action(call(FUNCTION_SURNAME)))
                    .end(operation().action(call(JAVA_FUNCTION))).build();

            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(workflow);
            // execute it with one person name
            logger.info(application.execute(process, Map.of("name", "Javier", "surname", "Tirado")).getWorkflowdata().toPrettyString());
            // execute it with other person name
            logger.info(application.execute(process, Map.of("name", "Mark", "surname", "Proctor")).getWorkflowdata().toPrettyString());
        }
    }
}
