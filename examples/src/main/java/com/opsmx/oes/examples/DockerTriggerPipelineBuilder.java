package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.json.triggers.DockerTrigger;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.List;
import java.util.Map;

public class DockerTriggerPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "dockerTriggerPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
            + "/../config.properties", System.getProperty("user.dir")
            + "/../config-overide.properties");
        final String dockerRegistryName = config.getConfigData("dockerRegistryName");
        final String dockerAccountName = config.getConfigData("dockerAccountName");
        final String dockerRegistryOrganization = config.getConfigData("dockerRegistryOrganization");
        final String dockerRegistryImage = config.getConfigData("dockerRegistryImage");
        final String shortWaitStagePeriod = config.getConfigData("shortWaitStagePeriod");

        Stage waitStage = Stage.builder()
                .type(StageTypes.WAIT)
                .name("Wait")
                .continuePipeline(true)
                .context(Map.of("waitTime", shortWaitStagePeriod))
                .build();

        Pipeline pipeline = Pipeline.builder()
                                    .name(pipelineName)
                                    .stages(List.of(waitStage))
                                    .trigger(DockerTrigger.builder()
                                                        .registry(dockerRegistryName)
                                                        .account(dockerAccountName)
                                                        .organization(dockerRegistryOrganization)
                                                        .repository(dockerRegistryImage)
                                                        .runAsUser("spinnakertestsuite")
                                                        .enabled(true)
                                                        .build())
                                    .build();
        pipeline.setApplication(appName);
        return pipeline;
    }
}
