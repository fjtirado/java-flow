package org.kie.kogito.serverless.workflow.examples;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.kie.kogito.serverless.workflow.executor.StaticWorkflowApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.TextNode;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.fluent.ActionBuilder.call;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.expr;
import static org.kie.kogito.serverless.workflow.fluent.FunctionBuilder.java;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.forEach;
import static org.kie.kogito.serverless.workflow.fluent.StateBuilder.operation;
import static org.kie.kogito.serverless.workflow.fluent.WorkflowBuilder.workflow;

public class ForEachJavaExample {

    private static final String GET_MESSAGE_FROM_FILE = "GET_MESSAGE";
    private static final String CONCAT_FUNCTION = "CONCAT";

    private static final Logger logger = LoggerFactory.getLogger(ForEachJavaExample.class);

    public static void main(String[] args) {
        try (StaticWorkflowApplication application = StaticWorkflowApplication.create()) {

            // this flow illustrate the usage of foreach and how to use java to perform task that are not part of sw spec.
            // The flow accepts a list of names and suffix them with a message read from a file 
            Workflow flow = workflow("ForEachExample")
                    // define java function that retrieve string from file
                    .function(java(GET_MESSAGE_FROM_FILE, ForEachJavaExample::addAdvice))
                    // jq expression that suffix each name with the message retrieved from the file
                    .function(expr(CONCAT_FUNCTION, ".name+.adviceMessage"))
                    // first load the message from the file and store it in message property
                    .start(operation().action(call(GET_MESSAGE_FROM_FILE, new TextNode(".fileName"))))
                    // then for each element in input names concatenate it with that message
                    .end(forEach(".names").loopVar("name").outputCollection(".messages").action(call(CONCAT_FUNCTION))
                            // only return messages list as result of the flow
                            .outputFilter("{messages}"))
                    .build();
            // execute the flow passing the list of names and the file name
            logger.info(application.execute(flow, Map.of("names", Arrays.asList("Javi", "Mark", "Kris", "Alessandro"), "fileName", "message.txt")).getWorkflowdata().toPrettyString());
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
