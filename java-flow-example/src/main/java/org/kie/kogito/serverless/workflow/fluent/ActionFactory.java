package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.List;

import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.branches.Branch;
import io.serverlessworkflow.api.filters.ActionDataFilter;
import io.serverlessworkflow.api.functions.FunctionRef;
import io.serverlessworkflow.api.functions.SubFlowRef;

public class ActionFactory<T> {

    private T parent;
    private List<Action> actions = new ArrayList<>();
    private Action currentAction;

    protected ActionFactory(T parent, List<Action> actions) {
        this.parent = parent;
        this.actions = actions;
    }

    protected ActionFactory(Branch branch) {
        branch.withActions(actions);
    }

    public ActionFactory<T> functionCall(String functionName) {
        return functionCall(functionName, NullNode.instance);
    }

    public ActionFactory<T> functionCall(String functionName, JsonNode args) {
        currentAction = new Action().withFunctionRef(new FunctionRef().withRefName(functionName).withArguments(args));
        actions.add(currentAction);
        return this;
    }

    public ActionFactory<T> subprocess(org.kie.kogito.process.Process<JsonNodeModel> subprocess) {
        currentAction = new Action().withSubFlowRef(new SubFlowRef().withWorkflowId(subprocess.id()));
        actions.add(currentAction);
        return this;
    }

    private ActionDataFilter getFilter() {
        ActionDataFilter actionFilter = null;
        if (currentAction != null) {
            actionFilter = currentAction.getActionDataFilter();
            if (actionFilter == null) {
                actionFilter = new ActionDataFilter();
                currentAction.withActionDataFilter(actionFilter);
            }
        }
        return actionFilter;
    }

    public ActionFactory<T> noResult() {
        ActionDataFilter filter = getFilter();
        if (filter != null) {
            filter.withUseResults(false);
        }
        return this;
    }

    public ActionFactory<T> inputFilter(String expr) {
        ActionDataFilter filter = getFilter();
        if (filter != null) {
            filter.withFromStateData(expr);
        }
        return this;
    }

    public ActionFactory<T> resultFilter(String expr) {
        ActionDataFilter filter = getFilter();
        if (filter != null) {
            filter.withResults(expr);
        }
        return this;
    }

    public ActionFactory<T> outputFilter(String expr) {
        ActionDataFilter filter = getFilter();
        if (filter != null) {
            filter.withToStateData(expr);
        }
        return this;
    }

    public T other() {
        return parent;
    }

}
