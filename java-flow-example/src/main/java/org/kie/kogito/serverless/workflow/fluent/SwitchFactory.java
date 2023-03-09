package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.List;

import io.serverlessworkflow.api.defaultdef.DefaultConditionDefinition;
import io.serverlessworkflow.api.states.SwitchState;
import io.serverlessworkflow.api.switchconditions.DataCondition;

public class SwitchFactory extends StateFactory<SwitchFactory> {

    private SwitchState switchState;
    private List<DataCondition> conditions = new ArrayList<>();

    protected SwitchFactory(StateFactory<?> stateFactory, SwitchState switchState) {
        super(stateFactory.states);
        this.switchState = switchState;
        switchState.withDataConditions(conditions);
    }

    public SwitchFactory ifThen(String conditionExpr) {
        pendingCondition = new DataCondition().withCondition(conditionExpr);
        conditions.add((DataCondition) pendingCondition);
        return this;
    }

    public SwitchFactory orElse() {
        pendingCondition = new DefaultConditionDefinition();
        switchState.withDefaultCondition((DefaultConditionDefinition) pendingCondition);
        return this;
    }
}
