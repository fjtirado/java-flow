package org.kie.kogito.serverless.workflow.fluent;

import io.serverlessworkflow.api.filters.ActionDataFilter;
import io.serverlessworkflow.api.filters.StateDataFilter;

public class DataFilterFactory {
    public static StateDataFilter inputFilter(String filter) {
        return new StateDataFilter().withInput(filter);
    }

    public static StateDataFilter outputFilter(String filter) {
        return new StateDataFilter().withOutput(filter);
    }

    public static StateDataFilter stateFilter(String input, String output) {
        return new StateDataFilter().withInput(input).withOutput(output);
    }

    public static ActionDataFilter noResult() {
        return new ActionDataFilter().withUseResults(false);
    }

    public static ActionDataFilter fromFilter(String from) {
        return new ActionDataFilter().withFromStateData(from);
    }

    public static ActionDataFilter resultFilter(String result) {
        return new ActionDataFilter().withResults(result);
    }

    public static ActionDataFilter toFilter(String result) {
        return new ActionDataFilter().withToStateData(result);
    }

    public static ActionDataFilter actionFilter(String from, String data, String to) {
        return new ActionDataFilter().withFromStateData(from).withResults(data).withToStateData(to);
    }
}
