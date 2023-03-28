package org.kie.kogito.serverless.workflow.examples;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

import org.kie.kogito.serverless.workflow.utils.WorkflowFormat;

import io.serverlessworkflow.api.Workflow;

import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.getWorkflow;
import static org.kie.kogito.serverless.workflow.utils.ServerlessWorkflowUtils.writeWorkflow;

public class FlowWriter {
    // This methods shows how to write the flow to a file and read it from such file, which might be useful to reuse the flow somewhere else
    static Workflow writeToFile(Workflow flow, String fileName) {

        try (Writer writer = new FileWriter(fileName)) {
            writeWorkflow(flow, writer, WorkflowFormat.JSON);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }

        try (Reader reader = new FileReader(fileName)) {
            flow = getWorkflow(reader, WorkflowFormat.JSON);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }

        return flow;
    }
}
