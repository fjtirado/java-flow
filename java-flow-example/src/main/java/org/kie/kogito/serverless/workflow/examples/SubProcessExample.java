package org.kie.kogito.serverless.workflow.examples;

import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.HttpMethod;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.subprocess;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.rest;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.parallel;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.objectNode;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class SubProcessExample {

    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String PERSON_COUNTRY_ID_FUNCTION = "GET_COUNTRY_ID";
    private static final String COUNTRY_ID_NAME_FUNCTION = "GET_COUNTRY_NAME";
    private static final String GENDER_FUNCTION = "GET_GENDER";

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {

            ObjectNode nameArgs = objectNode().put("name", ".name");
            Process<JsonNodeModel> subprocess = application.process(workflow("GetCountry")
                    .function(rest(PERSON_COUNTRY_ID_FUNCTION, HttpMethod.get, "https://api.nationalize.io:/?name={name}"))
                    .function(rest(COUNTRY_ID_NAME_FUNCTION, HttpMethod.get, "https://restcountries.com/v3.1/alpha/{id}"))
                    .singleton(operation().outputFilter("{country}")
                            .action(call(PERSON_COUNTRY_ID_FUNCTION, nameArgs).resultFilter(".country[0].country_id").outputFilter(".id"))
                            .action(call(COUNTRY_ID_NAME_FUNCTION, objectNode().put("id", ".id"))
                                    .resultFilter("{country: {name:.[].name.common, latitude: .[].latlng[0], longitude: .[].latlng[1] }}"))));

            Workflow flow = workflow("SubprocessExample").function(rest(AGE_FUNCTION, HttpMethod.get, "https://api.agify.io/?name={name}"))
                    .function(rest(GENDER_FUNCTION, HttpMethod.get, "https://api.genderize.io/?name={name}"))
                    .singleton(parallel().newBranch().action(call(AGE_FUNCTION, nameArgs).resultFilter("{age}")).endBranch()
                            .newBranch().action(subprocess(subprocess)).endBranch()
                            .newBranch().action(call(GENDER_FUNCTION, nameArgs).resultFilter("{gender}")).endBranch());
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier")));
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Ricardo")));
        }
    }

}
