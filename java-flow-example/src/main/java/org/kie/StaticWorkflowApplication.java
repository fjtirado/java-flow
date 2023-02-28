package org.kie;

import org.kie.kogito.KogitoEngine;
import org.kie.kogito.StaticApplication;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.Processes;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

public class StaticWorkflowApplication extends StaticApplication {

    private static final StaticWorkflowApplication INSTANCE = new StaticWorkflowApplication();

    private final StaticWorkflowProcesses processes = new StaticWorkflowProcesses();

    public static StaticWorkflowApplication get() {
        return INSTANCE;
    }

    public StaticWorkflowApplication addProcess(Process<JsonNodeModel> process) {
        processes.addProcess(process);
        return this;
    }

    @Override
    public <T extends KogitoEngine> T get(Class<T> clazz) {
        if (Processes.class.isAssignableFrom(clazz)) {
            return clazz.cast(processes);
        }
        return super.get(clazz);
    }

}
