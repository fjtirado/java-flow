package org.kie.kogito.serverless.workflow.fluent;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.kie.kogito.jackson.utils.JsonObjectUtils;
import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.workflow.Constants;
import io.serverlessworkflow.api.workflow.Functions;

public class WorkflowFactory extends StateFactory<WorkflowFactory> {

    private static ObjectMapper mapper = ObjectMapperFactory.get();

    public static WorkflowFactory workflow(String id) {
        return workflow(id, "1_0");
    }

    public static WorkflowFactory workflow(String id, String version) {
        return new WorkflowFactory(id, version);
    }

    private Workflow workflow;
    private List<FunctionDefinition> functions = new LinkedList<>();

    private WorkflowFactory(String id, String version) {
        super(new LinkedList<>());
        this.workflow = new Workflow().withId(id).withVersion(version);

    }

    public WorkflowFactory name(String name) {
        workflow.withName(name);
        return this;
    }

    public WorkflowFactory constant(String name, Object value) {
        Constants constants = workflow.getConstants();

        if (constants == null) {
            constants = new Constants(new ObjectMapper().createObjectNode());
            workflow.setConstants(constants);
        }
        ((ObjectNode) constants.getConstantsDef()).set(name, JsonObjectUtils.fromValue(value));
        return this;
    }

    public Workflow build() {
        workflow.withStart(new Start().withStateName(states.getFirst().getName()));
        workflow.withStates((List) states);
        workflow.withFunctions(new Functions(functions));
        End end = new End();
        for (DefaultState state : states) {
            if (state.getType() != Type.SWITCH && state.getTransition() == null && state.getEnd() == null) {
                state.setEnd(end);
            }
        }

        return workflow;
    }

    public void done() {

    }

    public final WorkflowFactory function(String functionName, FunctionDefinition.Type type, String operation) {
        return function(functionName, type, operation, c -> {
        });
    }

    public final WorkflowFactory function(String functionName, FunctionDefinition.Type type, String operation, Consumer<FunctionDefinition> consumer) {
        FunctionDefinition functionDef = new FunctionDefinition(functionName).withType(type).withOperation(operation);
        consumer.accept(functionDef);
        functions.add(functionDef);
        return this;
    }

    public static ObjectNode args() {
        return mapper.createObjectNode();
    }

}
