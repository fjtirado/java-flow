package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.actions.WorkflowLogLevel;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.log;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class ExpressionExample {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionExample.class);

    public static void main(String[] args) {

        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            // This flow illustrate the usage of two consecutive function calls
            Workflow workflow = workflow("ExpressionExample")
                    // concatenate name 
                    .start(operation()
                            .action(call(expr("name", "\"My name is \"+.name")))
                            // you can add several sequential actions into an operation
                            .action(log(WorkflowLogLevel.DEBUG, "\"Response is\"+.response")))
                    // concatenate surname
                    .next(operation()
                            .action(call(expr("surname", ".response+\" and my surname is \"+.surname")))
                            .outputFilter(".response"))
                    .end().build();

            //            workflow = FlowWriter.writeToFile(workflow, "expression.sw.json");

            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(workflow);
            // execute it with one person name
            logger.info(application.execute(process, Map.of("name", "Javier", "surname", "Tirado")).getWorkflowdata().toPrettyString());
            // execute it with other person name
            logger.info(application.execute(process, Map.of("name", "Mark", "surname", "Proctor")).getWorkflowdata().toPrettyString());
        }
    }
}
