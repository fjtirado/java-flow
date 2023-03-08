package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

import static org.kie.kogito.serverless.workflow.fluent.DataFilterFactory.outputFilter;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.objectNode;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.workflow;

public class SubProcessExample {

    private static final String START_STATE = "START";
    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String PERSON_COUNTRY_ID_FUNCTION = "GET_COUNTRY_ID";
    private static final String COUNTRY_ID_NAME_FUNCTION = "GET_COUNTRY_NAME";
    private static final String GENDER_FUNCTION = "GET_GENDER";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {

            ObjectNode nameArgs = objectNode().put("name", ".name");
            Process<JsonNodeModel> subprocess = application.process(workflow("GetCountry")
                    .function(PERSON_COUNTRY_ID_FUNCTION, Type.CUSTOM, "rest:get:https://api.nationalize.io:80/?name={name}")
                    .function(COUNTRY_ID_NAME_FUNCTION, Type.CUSTOM, "rest:get:https://restcountries.com:80/v3.1/alpha/{id}")
                    .operation(START_STATE,
                            actionFactory -> actionFactory.functionCall(PERSON_COUNTRY_ID_FUNCTION, nameArgs).resultFilter(".country[0].country_id").outputFilter(".id")
                                    .functionCall(COUNTRY_ID_NAME_FUNCTION, objectNode().put("id", ".id"))
                                    .resultFilter("{country: {name:.[].name.common, latitude: .[].latlng[0], longitude: .[].latlng[1] }}"),
                            state -> state.withStateDataFilter(outputFilter("{country}")))
                    .build());

            Workflow flow = workflow("FullExample").function(AGE_FUNCTION, Type.CUSTOM, "rest:get:https://api.agify.io:80/?name={name}")
                    .function(GENDER_FUNCTION, Type.CUSTOM, "rest:get:https://api.genderize.io:80/?name={name}")
                    .parallel(START_STATE,
                            branchFactory -> branchFactory.branch().functionCall(AGE_FUNCTION, nameArgs).resultFilter("{age}").other()
                                    .branch().subprocess(subprocess).other()
                                    .branch().functionCall(GENDER_FUNCTION, nameArgs).resultFilter("{gender}"))
                    .build();
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")));
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Ricardo")));
        }
    }

}
