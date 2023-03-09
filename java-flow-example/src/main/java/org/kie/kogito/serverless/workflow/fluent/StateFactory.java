package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.defaultdef.DefaultConditionDefinition;
import io.serverlessworkflow.api.end.End;
import io.serverlessworkflow.api.interfaces.State;
import io.serverlessworkflow.api.states.DefaultState;
import io.serverlessworkflow.api.states.DefaultState.Type;
import io.serverlessworkflow.api.states.InjectState;
import io.serverlessworkflow.api.states.OperationState;
import io.serverlessworkflow.api.states.ParallelState;
import io.serverlessworkflow.api.states.SwitchState;
import io.serverlessworkflow.api.switchconditions.DataCondition;
import io.serverlessworkflow.api.transitions.Transition;

public class StateFactory<T extends StateFactory<T>> {

    public final T inject(String name, JsonNode data) {
        return inject(name, data, c -> {
        });
    }

    protected final Deque<DefaultState> states;
    protected Object pendingCondition;

    public StateFactory(Deque<DefaultState> states) {
        this.states = states;
    }

    public final T split(String name, Consumer<SwitchFactory> factoryConsumer) {
        return split(name, factoryConsumer, c -> {
        });
    }

    public final T split(String name, Consumer<SwitchFactory> factoryConsumer, Consumer<SwitchState> stateConsumer) {
        SwitchState state = new SwitchState(name, Type.SWITCH);
        processState(state, stateConsumer);
        SwitchFactory switchFactory = new SwitchFactory(this, state);
        factoryConsumer.accept(switchFactory);
        return (T) this;
    }

    public final T inject(String name, JsonNode data, Consumer<InjectState> consumer) {
        return processState(new InjectState(name, Type.INJECT).withData(data), consumer);
    }

    public final T operation(String name, Consumer<ActionFactory<OperationState>> actionFactoryConsumer, Consumer<OperationState> consumer) {
        List<Action> actions = new ArrayList<>();
        OperationState state = new OperationState().withName(name).withType(Type.OPERATION).withActions(actions);
        ActionFactory<OperationState> actionFactory = new ActionFactory<>(state, actions);
        actionFactoryConsumer.accept(actionFactory);
        processState(state, consumer);
        return (T) this;
    }

    public final T parallel(String name, Consumer<BranchFactory> branchFactoryConsumer) {
        return parallel(name, branchFactoryConsumer, c -> {
        });
    }

    public final T parallel(String name, Consumer<BranchFactory> actionFactoryConsumer, Consumer<ParallelState> consumer) {
        ParallelState state = new ParallelState().withName(name).withType(Type.PARALLEL);
        BranchFactory actionFactory = new BranchFactory(state);
        actionFactoryConsumer.accept(actionFactory);
        processState(state, consumer);
        return (T) this;
    }

    public final T operation(String name, Consumer<ActionFactory<OperationState>> actionFactory) {
        return operation(name, actionFactory, c -> {
        });
    }

    private <S extends DefaultState> T processState(S state, Consumer<S> consumer) {
        consumer.accept(state);
        transition(state);
        states.add(state);
        return (T) this;
    }

    protected void transition(State state) {
        Transition transition = new Transition(state.getName());
        if (pendingCondition instanceof DataCondition) {
            ((DataCondition) pendingCondition).withTransition(transition);
        } else if (pendingCondition instanceof DefaultConditionDefinition) {
            ((DefaultConditionDefinition) pendingCondition).withTransition(transition);
        } else if (!states.isEmpty()) {
            states.getLast().withTransition(transition);
        }
    }

    public T end() {
        End end = new End();
        if (pendingCondition instanceof DataCondition) {
            ((DataCondition) pendingCondition).withEnd(end);
        } else if (pendingCondition instanceof DefaultConditionDefinition) {
            ((DefaultConditionDefinition) pendingCondition).withEnd(end);
        } else if (!states.isEmpty()) {
            states.getLast().setEnd(end);
        }
        return (T) this;
    }

}
