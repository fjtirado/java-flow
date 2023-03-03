package org.kie.kogito.serverless.workflow.examples;

import java.util.Arrays;
import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.branches.Branch;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.*;

public class ParallelServices {

    private static final String START_STATE = "START";
    private static final String AGE_FUNCTION = "GET_AGE";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow flow = workflow("ParallelServices").function(AGE_FUNCTION, Type.CUSTOM, "rest:get:https://api.agify.io:80/")
                    .parallel(START_STATE, state -> state.withBranches(Arrays.asList(new Branch())))
                    .operation(START_STATE, actionFactory -> actionFactory.functionCall(AGE_FUNCTION, objectNode().put("QUERY_name", ".name"))).build();
            // there is JIRA https://issues.redhat.com/browse/KOGITO-8779 to be able to write
            // "rest:get:https://api.agify.io:80/?name={name}"
            // objectNode().put("name", ".name"))
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")));
        }
    }

}
