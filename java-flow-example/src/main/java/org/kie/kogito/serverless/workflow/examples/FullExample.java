package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.functions.FunctionDefinition.Type;

import static org.kie.kogito.serverless.workflow.fluent.DataFilterFactory.outputFilter;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.args;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowFactory.workflow;

public class FullExample {

    private static final String START_STATE = "START";
    private static final String CHECK_AGE_STATE = "CHECK_AGE";
    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String PERSON_COUNTRY_ID_FUNCTION = "GET_COUNTRY_ID";
    private static final String COUNTRY_ID_NAME_FUNCTION = "GET_COUNTRY_NAME";
    private static final String GENDER_FUNCTION = "GET_GENDER";
    private static final String UNIVERSITY_FUNCTION = "GET_UNIVERSITY";
    private static final String WEATHER_FUNCTION = "GET_WEATHER";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {

            ObjectNode nameArgs = args().put("name", ".name");
            Process<JsonNodeModel> subprocess = application.process(workflow("GetCountry")
                    .function(PERSON_COUNTRY_ID_FUNCTION, Type.CUSTOM, "rest:get:https://api.nationalize.io:80/?name={name}")
                    .function(COUNTRY_ID_NAME_FUNCTION, Type.CUSTOM, "rest:get:https://restcountries.com:80/v3.1/alpha/{id}")
                    .operation(START_STATE,
                            actionFactory -> actionFactory.functionCall(PERSON_COUNTRY_ID_FUNCTION, nameArgs).resultFilter(".country[0].country_id").outputFilter(".id")
                                    .functionCall(COUNTRY_ID_NAME_FUNCTION, args().put("id", ".id"))
                                    .resultFilter("{country: {name:.[].name.common, latitude: .[].latlng[0], longitude: .[].latlng[1] }}"),
                            state -> state.withStateDataFilter(outputFilter("{country}")))
                    .build());

            Workflow flow = workflow("FullExample").function(AGE_FUNCTION, Type.CUSTOM, "rest:get:https://api.agify.io:80/?name={name}")
                    .constant("apiKey", "2482c1d33308a7cffedff5764e9ef203")
                    .function(GENDER_FUNCTION, Type.CUSTOM, "rest:get:https://api.genderize.io:80/?name={name}")
                    .function(UNIVERSITY_FUNCTION, Type.CUSTOM, "rest:get:http://universities.hipolabs.com:80/search?country={country}")
                    .function(WEATHER_FUNCTION, Type.CUSTOM, "rest:get:https://api.openweathermap.org:80/data/2.5/weather?lat={lat}&lon={lon}&appid={appid}")
                    .parallel(START_STATE,
                            branchFactory -> branchFactory.branch().functionCall(AGE_FUNCTION, nameArgs).resultFilter("{age}").other()
                                    .branch().subprocess(subprocess).other()
                                    .branch().functionCall(GENDER_FUNCTION, nameArgs).resultFilter("{gender}"))
                    .split(CHECK_AGE_STATE, factory -> factory
                            .ifThen(".age<50")
                            .operation(UNIVERSITY_FUNCTION, actionFactory -> actionFactory
                                    .functionCall(UNIVERSITY_FUNCTION, args().put("country", ".country.name"))
                                    .resultFilter(".[].name").outputFilter(".universities"))
                            .orElse()
                            .operation(WEATHER_FUNCTION, actionFactory -> actionFactory
                                    .functionCall(WEATHER_FUNCTION, args().put("lat", ".country.latitude").put("lon", ".country.longitude").put("appid", "$CONST.apiKey"))
                                    .resultFilter("{weather:.main}")))
                    .build();
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")).getWorkflowdata());
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Alba")).getWorkflowdata());
        }
    }

}
