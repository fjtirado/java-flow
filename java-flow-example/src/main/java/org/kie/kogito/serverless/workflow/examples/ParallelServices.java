package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.HttpMethod;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.rest;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.parallel;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.objectNode;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class ParallelServices {

    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String COUNTRY_FUNCTION = "GET_COUNTRY";
    private static final String GENDER_FUNCTION = "GET_GENDER";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow flow = workflow("ParallelServices").function(rest(AGE_FUNCTION, HttpMethod.get, "https://api.agify.io/"))
                    .function(rest(COUNTRY_FUNCTION, HttpMethod.get, "https://api.nationalize.io/"))
                    .function(rest(GENDER_FUNCTION, HttpMethod.get, "https://api.genderize.io/"))
                    .singleton(parallel().newBranch().action(call(AGE_FUNCTION, objectNode().put("QUERY_name", ".name")).resultFilter(".age").outputFilter(".age")).endBranch()
                            .newBranch().action(call(COUNTRY_FUNCTION, objectNode().put("QUERY_name", ".name")).resultFilter(".country[].country_id").outputFilter(".country")).endBranch()
                            .newBranch().action(call(GENDER_FUNCTION, objectNode().put("QUERY_name", ".name")).resultFilter(".gender").outputFilter(".gender")).endBranch());
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")));
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Vani")));
        }
    }

}
