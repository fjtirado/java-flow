package org.kie.kogito.serverless.workflow.fluent;

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
}
