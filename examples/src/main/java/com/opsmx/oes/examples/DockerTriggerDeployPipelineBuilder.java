package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.json.triggers.DockerTrigger;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.*;

public class DockerTriggerDeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "dockerTriggerDeployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String k8sAccountName = config.getConfigData("k8sAccountName");
        final String k8sNamespace = config.getConfigData("k8sNamespace");
        final String dockerImageName = config.getConfigData("dockerImageName");
        final String dockerRegistryName = config.getConfigData("dockerRegistryName");
        final String dockerAccountName = config.getConfigData("dockerAccountName");
        final String dockerRegistryOrganization = config.getConfigData("dockerRegistryOrganization");
        final String dockerRegistryImage = config.getConfigData("dockerRegistryImage");

        Stage deployManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name("Deploy_manifest")
                .continuePipeline(true)
                .context(generateDeploymentObject(appName, k8sAccountName, k8sNamespace))
                .build();

        Stage deleteManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DELETE_MANIFEST)
                .name("Delete (Manifest)")
                .continuePipeline(true)
                .parentStageId(deployManifestStage.getId())
                .context(generateDeleteManifestObject(k8sAccountName, k8sNamespace))
                .build();

        Pipeline pipeline = Pipeline.builder()
                .name(pipelineName)
                .stages(List.of(deployManifestStage, deleteManifestStage))
                .trigger(DockerTrigger.builder()
                        .registry("index.docker.io")
                        .account("dockerhub")
                        .organization(dockerRegistryOrganization)
                        .repository("library/ubuntu")
                        .runAsUser("spinnakertestsuite")
                        .enabled(true)
                        .build())
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateDeploymentObject(final String appName, final String k8sAccountName,
                                                         final String k8sNamespace) {
        Map<String, Object> deploymentManifest = new HashMap<>();

        deploymentManifest.put("account", k8sAccountName);
        deploymentManifest.put("cloudProvider", "kubernetes");

        List<Map<String, Object>> manifests = new ArrayList<>();
        Map<String, Object> deploymentSpec = createDeploymentSpec();
        manifests.add(deploymentSpec);

        deploymentManifest.put("manifests", manifests);

        Map<String, String> moniker = new HashMap<>();
        moniker.put("app", appName);
        deploymentManifest.put("moniker", moniker);

        deploymentManifest.put("name", "Deploy (Manifest)");
        deploymentManifest.put("namespaceOverride", k8sNamespace);

        List<Map<String, Object>> requiredArtifacts = new ArrayList<>();
        Map<String, Object> artifact = createArtifact();
        Map<String, Object> requiredArtifact = new HashMap<>();
        requiredArtifact.put("artifact", artifact);
        requiredArtifacts.add(requiredArtifact);

        deploymentManifest.put("requiredArtifacts", requiredArtifacts);
        deploymentManifest.put("requisiteStageRefIds", new ArrayList<>());
        deploymentManifest.put("skipExpressionEvaluation", false);
        deploymentManifest.put("source", "text");

        Map<String, Object> trafficManagement = new HashMap<>();
        trafficManagement.put("enabled", false);
        Map<String, Object> options = new HashMap<>();
        options.put("enableTraffic", false);
        options.put("services", new ArrayList<>());
        trafficManagement.put("options", options);
        deploymentManifest.put("trafficManagement", trafficManagement);

        deploymentManifest.put("type", "deployManifest");

        return deploymentManifest;
    }

    private static Map<String, Object> createDeploymentSpec() {
        Map<String, Object> deploymentSpec = new HashMap<>();

        deploymentSpec.put("apiVersion", "apps/v1");
        deploymentSpec.put("kind", "Deployment");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "${trigger.repository.split('/')[1]}-deployment");
        deploymentSpec.put("metadata", metadata);

        Map<String, Object> spec = new HashMap<>();
        spec.put("replicas", 1);

        Map<String, Object> selector = new HashMap<>();
        Map<String, Object> matchLabels = new HashMap<>();
        matchLabels.put("app", "${trigger.repository.split('/')[1]}");
        selector.put("matchLabels", matchLabels);
        spec.put("selector", selector);

        Map<String, Object> template = new HashMap<>();
        Map<String, Object> templateMetadata = new HashMap<>();
        Map<String, Object> labels = new HashMap<>();
        labels.put("app", "${trigger.repository.split('/')[1]}");
        templateMetadata.put("labels", labels);
        template.put("metadata", templateMetadata);

        Map<String, Object> templateSpec = new HashMap<>();
        List<Map<String, Object>> containers = new ArrayList<>();
        Map<String, Object> container = createContainer();
        containers.add(container);
        templateSpec.put("containers", containers);

        template.put("spec", templateSpec);
        spec.put("template", template);
        deploymentSpec.put("spec", spec);

        return deploymentSpec;
    }

    private static Map<String, Object> createContainer() {
        Map<String, Object> container = new HashMap<>();

        List<String> command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("sleep 60m");
        container.put("command", command);

        container.put("image", "${trigger.artifacts[0].name}");
        container.put("name", "${trigger.repository.split('/')[1]}");
        container.put("resources", new HashMap<>());
        container.put("terminationMessagePath", "/dev/termination-log");
        container.put("terminationMessagePolicy", "File");

        return container;
    }

    private static Map<String, Object> createArtifact() {
        Map<String, Object> artifact = new HashMap<>();

        artifact.put("artifactAccount", "docker-registry");
        artifact.put("id", "ec3fe6c8-b6e2-434c-bf40-4fa75fa44af8");
        artifact.put("name", "${trigger.artifacts[0].name}");
        artifact.put("reference", "${trigger.artifacts[0].name}:${trigger.artifacts[0].version}");
        artifact.put("type", "docker/image");
        artifact.put("version", "${trigger.artifacts[0].version}");

        return artifact;
    }

    private Map<String, Object> generateDeleteManifestObject(final String k8sAccountName, final String k8sNamespace) {
        Map<String, Object> deleteManifestData = new HashMap<>();
        deleteManifestData.put("account", k8sAccountName);
        deleteManifestData.put("app", "docker");
        deleteManifestData.put("cloudProvider", "kubernetes");
        deleteManifestData.put("location", k8sNamespace);

        String manifestName = "deployment ${trigger.repository.split('/')[1]}-deployment";
        deleteManifestData.put("manifestName", manifestName);
        deleteManifestData.put("mode", "static");
        deleteManifestData.put("name", "Delete (Manifest)");

        Map<String, Boolean> options = Collections.singletonMap("cascading", true);
        deleteManifestData.put("options", options);

        deleteManifestData.put("type", "deleteManifest");

        return deleteManifestData;
    }
}
