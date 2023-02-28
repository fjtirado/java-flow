package org.kie;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.Processes;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

public class StaticWorkflowProcesses implements Processes {

    private Map<String, Process<JsonNodeModel>> processes = new ConcurrentHashMap<>();

    @Override
    public Process<? extends Model> processById(String processId) {
        return processes.get(processId);
    }

    @Override
    public Collection<String> processIds() {
        return processes.keySet();
    }

    public StaticWorkflowProcesses addProcess(Process<JsonNodeModel> process) {
        processes.put(process.id(), process);
        return this;
    }

}
