package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.states.OperationState;

public class ActionFactory {

    private List<Action> actions = new ArrayList<>();

    protected ActionFactory(OperationState state) {
        state.withActions(actions);
    }

    public ActionFactory functionCall(String functionName) {
        return functionCall(functionName, NullNode.instance);
    }

    public ActionFactory functionCall(String functionName, JsonNode args) {
        actions.add(new Action().withFunctionRef(new FunctionRef().withRefName(functionName).withArguments(args)));
        return this;
    }
}
