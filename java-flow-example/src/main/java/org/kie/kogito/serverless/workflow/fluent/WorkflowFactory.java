package org.kie.kogito.serverless.workflow.fluent;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.Workflow;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.start.Start;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.InjectState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.transitions.Transition;

public class WorkflowFactory {

    public static WorkflowFactory workflow(String id) {
        return workflow(id, "1_0");
    }

    public static WorkflowFactory workflow(String id, String version) {
        return new WorkflowFactory(id, version);
    }

    private Workflow workflow;
    private Deque<DefaultState> states = new LinkedList<>();

    private WorkflowFactory(String id, String version) {
        this.workflow = new Workflow().withId(id).withVersion(version);

    }

    public WorkflowFactory name(String name) {
        workflow.withName(name);
        return this;
    }

    @SafeVarargs
    public final WorkflowFactory inject(String name, JsonNode data, Consumer<InjectState>... consumers) {
        return processState(new InjectState(name, Type.INJECT).withData(data), consumers);
    }

    @SafeVarargs
    public final WorkflowFactory operation(String name, Consumer<OperationState>... consumers) {
        return processState(new OperationState().withName(name).withType(Type.OPERATION), consumers);
    }

    private <T extends DefaultState> WorkflowFactory processState(T state, Consumer<T>... consumers) {
        for (Consumer<T> consumer : consumers)
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
        return workflow;
    }

}
