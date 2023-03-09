package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.args;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.workflow;

public class ParallelServices {

    private static final String START_STATE = "START";
    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String COUNTRY_FUNCTION = "GET_COUNTRY";
    private static final String GENDER_FUNCTION = "GET_GENDER";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            Workflow flow = workflow("ParallelServices").function(AGE_FUNCTION, Type.CUSTOM, "rest:get:https://api.agify.io:80/")
                    .function(COUNTRY_FUNCTION, Type.CUSTOM, "rest:get:https://api.nationalize.io:80/")
                    .function(GENDER_FUNCTION, Type.CUSTOM, "rest:get:https://api.genderize.io:80/")
                    .parallel(START_STATE,
                            branchFactory -> branchFactory.branch().functionCall(AGE_FUNCTION, args().put("QUERY_name", ".name")).resultFilter(".age").outputFilter(".age").other()
                                    .branch().functionCall(COUNTRY_FUNCTION, args().put("QUERY_name", ".name")).resultFilter(".country[].country_id").outputFilter(".country").other()
                                    .branch().functionCall(GENDER_FUNCTION, args().put("QUERY_name", ".name")).resultFilter(".gender").outputFilter(".gender"))
                    .build();
            // there is JIRA https://issues.redhat.com/browse/KOGITO-8779 to be able to write
            // "rest:get:https://api.agify.io:80/?name={name}"
            // objectNode().put("name", ".name"))
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")));
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Vani")));
        }
    }

}
