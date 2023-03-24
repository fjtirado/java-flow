package org.kie.kogito.serverless.workflow.examples;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.actions.WorkflowLogLevel;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.HttpMethod;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.kie.kogito.serverless.workflow.utils.WorkflowFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.log;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.subprocess;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.log;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.rest;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.parallel;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.objectNode;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.getWorkflow;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.writeWorkflow;

public class FullExample {

    private static final Logger logger = LoggerFactory.getLogger(FullExample.class);

    private static final String AGE_FUNCTION = "GET_AGE";
    private static final String PERSON_COUNTRY_ID_FUNCTION = "GET_COUNTRY_ID";
    private static final String COUNTRY_ID_NAME_FUNCTION = "GET_COUNTRY_NAME";
    private static final String GENDER_FUNCTION = "GET_GENDER";
    private static final String UNIVERSITY_FUNCTION = "GET_UNIVERSITY";
    private static final String WEATHER_FUNCTION = "GET_WEATHER";
    private static final String LOG_INFO = "LOG_INFO";

    public static void main(String[] args) throws IOException {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {
            ObjectNode nameArgs = objectNode().put("name", ".name");
            // Define a subflow process that retrieve country information from the given name
            Workflow subflow = workflow("GetCountry")
                    // define rest function to retrieve country id
                    .function(rest(PERSON_COUNTRY_ID_FUNCTION, HttpMethod.get, "https://api.nationalize.io:/?name={name}"))
                    // define rest function to retrieve country information from country id
                    .function(rest(COUNTRY_ID_NAME_FUNCTION, HttpMethod.get, "https://restcountries.com/v3.1/alpha/{id}"))
                    // subflow consist of just one state with two sequential actions
                    .singleton(operation()
                            // call rest function to retrieve country id 
                            .action(call(PERSON_COUNTRY_ID_FUNCTION, nameArgs)
                                    // extract relevant information from the response using JQ expression
                                    .resultFilter(".country[0].country_id").outputFilter(".id"))
                            // call rest function to retrieve country information from country id
                            .action(call(COUNTRY_ID_NAME_FUNCTION, objectNode().put("id", ".id"))
                                    // we are only interested in country name, longitude and latitude
                                    .resultFilter("{country: {name:.[].name.common, latitude: .[].latlng[0], longitude: .[].latlng[1] }}"))
                            // return only country field to parent flow
                            .outputFilter("{country}"));

            //subflow = writeToFile(subflow, "country.sw.json");

            Process<JsonNodeModel> subprocess = application.process(subflow);
            // This is the main flow, it invokes two services (one for retrieving the age and another to get the gender of the given name )and one subprocess (the country one defined above) in parallel
            // Once the three of them has been executed, if age is greater than 50, it retrieve the weather information for the retrieved country,
            // Else, it gets the list of universities for that country. 
            Workflow flow = workflow("FullExample")
                    // Api key to be used in getting weather call
                    .constant("apiKey", "2482c1d33308a7cffedff5764e9ef203")
                    // Age rest call definition
                    .function(rest(AGE_FUNCTION, HttpMethod.get, "https://api.agify.io/?name={name}"))
                    // Gender rest call definition
                    .function(rest(GENDER_FUNCTION, HttpMethod.get, "https://api.genderize.io/?name={name}"))
                    // University rest call definition
                    .function(rest(UNIVERSITY_FUNCTION, HttpMethod.get, "http://universities.hipolabs.com/search?country={country}"))
                    // Weather rest call definition
                    .function(rest(WEATHER_FUNCTION, HttpMethod.get, "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={appid}"))
                    // This defines a logger function set up with INFO level. 
                    .function(log(LOG_INFO, WorkflowLogLevel.INFO))
                    // Starts performing retrieval of gender, country and age from the given name on parallel 
                    .start(parallel()
                            .newBranch().action(call(AGE_FUNCTION, nameArgs).resultFilter("{age}")).endBranch()
                            .newBranch().action(subprocess(subprocess)).endBranch()
                            .newBranch().action(call(GENDER_FUNCTION, nameArgs).resultFilter("{gender}")).endBranch())
                    // once done, logs the age (using Jq string interpolation)
                    .next(operation().action(log(LOG_INFO, "\"Age is \\(.age)\"")))
                    // If age is less that fifty, retrieve the list of universities (the parameters object is built using jq expressions) 
                    .when(".age<50").end(operation().action(call(UNIVERSITY_FUNCTION, objectNode().put("country", ".country.name")).resultFilter(".[].name").outputFilter(".universities")))
                    // Else retrieve the weather for that country capital latitude and longitude (note how parameters are build from model info) 
                    .or().end(operation().action(call(WEATHER_FUNCTION, objectNode().put("lat", ".country.latitude").put("lon", ".country.longitude").put("appid", "$CONST.apiKey"))
                            .resultFilter("{weather:.main}")))
                    .build();

            //subflow = writeToFile(flow, "fullexample.sw.json");

            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            logger.info(application.execute(process, Map.of("name", "Javier")).getWorkflowdata().toPrettyString());
            // execute it with another person name
            logger.info(application.execute(process, Map.of("name", "Alba")).getWorkflowdata().toPrettyString());
        }
    }

    // This methods shows how to write the flow to a file and read it from such file, which might be useful to reuse the flow somewhere else
    private static Workflow writeToFile(Workflow flow, String fileName) throws IOException {
        try (Writer writer = new FileWriter(fileName)) {
            writeWorkflow(flow, writer, WorkflowFormat.JSON);
        }

        try (Reader reader = new FileReader(fileName)) {
            flow = getWorkflow(reader, WorkflowFormat.JSON);
        }
        return flow;
    }
}
