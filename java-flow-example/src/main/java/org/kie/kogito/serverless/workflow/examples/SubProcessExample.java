package org.kie.kogito.serverless.workflow.examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.HttpMethod;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.subprocess;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
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
    private static final String GET_MESSAGE_FROM_FILE = "GET_MESSAGE";

    private static final Logger logger = LoggerFactory.getLogger(SubProcessExample.class);

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
                    .function(java(GET_MESSAGE_FROM_FILE, SubProcessExample::addAdvice))
                    .function(rest(GENDER_FUNCTION, HttpMethod.get, "https://api.genderize.io/?name={name}"))
                    .start(parallel().newBranch().action(call(AGE_FUNCTION, nameArgs).resultFilter("{age}")).endBranch()
                            .newBranch().action(subprocess(subprocess)).endBranch()
                            .newBranch().action(call(GENDER_FUNCTION, nameArgs).resultFilter("{gender}")).endBranch())
                    .when(".country.name==\"Spain\" and .age>18 and .fileName")
                    .end(operation().action(call(GET_MESSAGE_FROM_FILE, new TextNode(".fileName"))))
                    .or().end(operation()).build();
            // create a reusable process for several executions
            Process<JsonNodeModel> process = application.process(flow);
            // execute it with one person name
            System.out.println(application.execute(process, Map.of("name", "Javier", "fileName", "message.txt")));
            // execute it with another person name
            System.out.println(application.execute(process, Map.of("name", "Ricardo")));
        }
    }

    // Java method invoked from workflow accepts one parameter, which might be a Map<String,Object) or a primitive/wrapper type, depending on the args provided in the flow
    // In this case, we are passing the name of a file  in the classpath
    // Java method return type is always a Map<String,Object> (if not output,it should return an empty map). In this case, 
    // we are adding an advice message to the flow model read from the file. If the file cannot be read, we return empty map.   
    private static Map<String, Object> addAdvice(String fileName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getSystemResourceAsStream(fileName)) {
            if (is != null) {
                return Collections.singletonMap("adviceMessage", new String(is.readAllBytes()));
            }
        } catch (IOException io) {
            logger.warn("Error reading file " + fileName + " from classpath", io);
        }
        return Collections.emptyMap();

    }

}
