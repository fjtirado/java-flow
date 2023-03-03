package org.kie.kogito.serverless.workflow.fluent;

import java.util.ArrayList;
import java.util.List;

import io.serverlessworkflow.api.actions.Action;
import io.serverlessworkflow.api.branches.Branch;
import io.serverlessworkflow.api.states.ParallelState;

public class BranchFactory {

    private List<Branch> branches = new ArrayList<>();

    protected BranchFactory(ParallelState state) {
        state.withBranches(branches);
    }

    public ActionFactory<BranchFactory> branch() {
        return branch("BRANCH-" + branches.size());
    }

    public ActionFactory<BranchFactory> branch(String name) {
        List<Action> actions = new ArrayList<>();
        Branch branch = new Branch().withName(name).withActions(actions);
        branches.add(branch);
        return new ActionFactory<>(this, actions);
    }

}
