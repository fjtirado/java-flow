package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.kie.kogito.jackson.utils.ObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.functions.FunctionDefinition;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.InjectState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.states.ParallelState;
import io.serverlessworkflow.api.transitions.Transition;
import io.serverlessworkflow.api.workflow.Functions;

public class WorkflowFactory {

    private static ObjectMapper mapper = ObjectMapperFactory.get();

    public static WorkflowFactory workflow(String id) {
        return workflow(id, "1_0");
    }

    public static WorkflowFactory workflow(String id, String version) {
        return new WorkflowFactory(id, version);
    }

    private Workflow workflow;
    private Deque<DefaultState> states = new LinkedList<>();
    private List<FunctionDefinition> functions = new LinkedList<>();

    private WorkflowFactory(String id, String version) {
        this.workflow = new Workflow().withId(id).withVersion(version);

    }

    public WorkflowFactory name(String name) {
        workflow.withName(name);
        return this;
    }

    public final WorkflowFactory inject(String name, JsonNode data) {
        return inject(name, data, c -> {
        });
    }

    public final WorkflowFactory inject(String name, JsonNode data, Consumer<InjectState> consumer) {
        return processState(new InjectState(name, Type.INJECT).withData(data), consumer);
    }

    public final WorkflowFactory operation(String name, Consumer<ActionFactory<OperationState>> actionFactoryConsumer, Consumer<OperationState> consumer) {
        List<Action> actions = new ArrayList<>();
        OperationState state = new OperationState().withName(name).withType(Type.OPERATION).withActions(actions);
        ActionFactory<OperationState> actionFactory = new ActionFactory<>(state, actions);
        actionFactoryConsumer.accept(actionFactory);
        processState(state, consumer);
        return this;
    }

    public final WorkflowFactory parallel(String name, Consumer<BranchFactory> branchFactoryConsumer) {
        return parallel(name, branchFactoryConsumer, c -> {
        });
    }

    public final WorkflowFactory parallel(String name, Consumer<BranchFactory> actionFactoryConsumer, Consumer<ParallelState> consumer) {
        ParallelState state = new ParallelState().withName(name).withType(Type.PARALLEL);
        BranchFactory actionFactory = new BranchFactory(state);
        actionFactoryConsumer.accept(actionFactory);
        processState(state, consumer);
        return this;
    }

    public final WorkflowFactory operation(String name, Consumer<ActionFactory<OperationState>> actionFactory) {
        return operation(name, actionFactory, c -> {
        });
    }

    private <T extends DefaultState> WorkflowFactory processState(T state, Consumer<T> consumer) {
        consumer.accept(state);
        if (!states.isEmpty()) {
            states.getLast().setTransition(new Transition().withNextState(state.getName()));
        }
        states.add(state);
        return this;
    }

    public Workflow build() {
        workflow.withStart(new Start().withStateName(states.getFirst().getName()));
        states.getLast().setEnd(new End());
        workflow.withStates((List) states);
        workflow.withFunctions(new Functions(functions));
        return workflow;
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

    public static ObjectNode objectNode() {
        return mapper.createObjectNode();
    }

}
