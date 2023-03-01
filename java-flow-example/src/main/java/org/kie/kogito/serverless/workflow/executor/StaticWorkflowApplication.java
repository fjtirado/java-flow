package org.kie.kogito.serverless.workflow.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.kogito.KogitoEngine;
import org.kie.kogito.Model;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.codegen.api.context.impl.JavaKogitoBuildContext;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.Processes;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;
import org.kie.kogito.serverless.workflow.parser.ServerlessWorkflowParser;

import io.serverlessworkflow.api.Workflow;

public class StaticWorkflowApplication extends StaticApplication {

    private static final StaticWorkflowApplication INSTANCE = new StaticWorkflowApplication();

    private final StaticWorkflowProcesses processes = new StaticWorkflowProcesses();

    public static StaticWorkflowApplication get() {
        return INSTANCE;
    }

    public Process<JsonNodeModel> process(Workflow workflow) {
        return processes.map.computeIfAbsent(getKey(workflow), k -> new StaticWorkflowProcess(this, ServerlessWorkflowParser
                .of(workflow, JavaKogitoBuildContext.builder().build()).getProcessInfo().info()));

    }

    private static String getKey(Workflow workflow) {
        return workflow.getId() + "_" + workflow.getVersion();
    }

    @Override
    public <T extends KogitoEngine> T get(Class<T> clazz) {
        if (Processes.class.isAssignableFrom(clazz)) {
            return clazz.cast(processes);
        }
        return super.get(clazz);
    }

    private class StaticWorkflowProcesses implements Processes {
        private Map<String, Process<JsonNodeModel>> map = new ConcurrentHashMap<>();

        @Override
        public Process<? extends Model> processById(String processId) {
            return map.get(processId);
        }

        @Override
        public Collection<String> processIds() {
            return map.keySet();
        }
    }

}
