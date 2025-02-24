package io.quarkiverse.mcp.server.stdio.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.quarkiverse.mcp.server.stdio.runtime.StdioMcpServerRecorder;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class StdioMcpServerProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("mcp-server-stdio");
    }

    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    @BuildStep
    void initialize(StdioMcpServerRecorder recorder) {
        recorder.initialize();
    }

}
