package com.opsmx.oes.examples;

import com.opsmx.oes.common.util.ConfigDataProvider;
import com.opsmx.oes.pipelinebuilder.json.Pipeline;
import com.opsmx.oes.pipelinebuilder.json.Stage;
import com.opsmx.oes.pipelinebuilder.json.notifications.EmailNotification;
import com.opsmx.oes.pipelinebuilder.json.notifications.NotificationEvent;
import com.opsmx.oes.pipelinebuilder.json.notifications.SlackNotification;
import com.opsmx.oes.pipelinebuilder.json.stages.model.StageTypes;
import com.opsmx.oes.pipelinebuilder.json.triggers.WebhookTrigger;
import com.opsmx.oes.pipelinebuilder.pipelines.JsonPipelineBuilder;

import java.util.*;

public class DockerDeployPipelineBuilder extends JsonPipelineBuilder {

    @Override
    public String getUniqueName() {
        return "dockerDeployPipeline";
    }

    @Override
    public Pipeline buildPipeline(final String appName, final String pipelineName) {

        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String k8sAccountName = config.getConfigData("k8sAccountName");
        final String k8sNamespace = config.getConfigData("k8sNamespace");
        final String dockerImageName = config.getConfigData("dockerImageName");

        Stage deployManifestStage = Stage.builder()
                .type(StageTypes.Kubernetes.DEPLOY_MANIFEST)
                .name("Deploy_manifest")
                .continuePipeline(true)
                .context(generateDeploymentObject(k8sAccountName, k8sNamespace, dockerImageName))
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
                .build();
        pipeline.setApplication(appName);
        return pipeline;
    }

    private Map<String, Object> generateDeploymentObject(final String k8sAccountName,
                                                         final String k8sNamespace, final String dockerImageName) {
        Map<String, Object> deployManifestData = new HashMap<>();
        deployManifestData.put("account", k8sAccountName);
        deployManifestData.put("cloudProvider", "kubernetes");

        Map<String, Object> manifestArtifact = getManifestArtifact();

        deployManifestData.put("manifestArtifact", manifestArtifact);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "simple-deploy");

        Map<String, Object> selector = new HashMap<>();
        selector.put("matchLabels", Collections.singletonMap("app", "simple-app1"));

        Map<String, Object> strategy = new HashMap<>();
        strategy.put("type", "RollingUpdate");

        Map<String, Object> templateLabels = Collections.singletonMap("app", "simple-app1");

        Map<String, Object> container = new HashMap<>();
        container.put("image", dockerImageName);
        container.put("imagePullPolicy", "Always");
        container.put("name", "restapp");

        Map<String, Object> httpPort = Collections.singletonMap("containerPort", 8080);
        Map<String, Object> prometheusPort = Collections.singletonMap("containerPort", 9090);

        container.put("ports", Arrays.asList(httpPort, prometheusPort));

        Map<String, Object> templateSpec = new HashMap<>();
        templateSpec.put("metadata", Collections.singletonMap("labels", templateLabels));
        templateSpec.put("spec", Collections.singletonMap("containers", Arrays.asList(container)));

        Map<String, Object> spec = new HashMap<>();
        spec.put("replicas", 1);
        spec.put("revisionHistoryLimit", 1);
        spec.put("selector", selector);
        spec.put("strategy", strategy);
        spec.put("template", templateSpec);

        Map<String, Object> deployment = new HashMap<>();
        deployment.put("apiVersion", "apps/v1");
        deployment.put("kind", "Deployment");
        deployment.put("metadata", metadata);
        deployment.put("spec", spec);

        List<Map<String, Object>> manifests = Arrays.asList(deployment);
        deployManifestData.put("manifests", manifests);

        Map<String, Object> moniker = Collections.singletonMap("app", "sampleapp");
        deployManifestData.put("moniker", moniker);

        deployManifestData.put("name", "Deploy (Manifest)");
        deployManifestData.put("namespaceOverride", k8sNamespace);
        deployManifestData.put("skipExpressionEvaluation", false);
        deployManifestData.put("source", "text");

        Map<String, Object> options = Collections.singletonMap("enableTraffic", false);
        Map<String, Object> trafficManagement = new HashMap<>();
        trafficManagement.put("enabled", false);
        trafficManagement.put("options", options);
        deployManifestData.put("trafficManagement", trafficManagement);
        deployManifestData.put("type", "deployManifest");

        return deployManifestData;
    }

    private static Map<String, Object> getManifestArtifact() {
        final ConfigDataProvider config = new ConfigDataProvider(System.getProperty("user.dir")
                + "/../config.properties", System.getProperty("user.dir")
                + "/../config-overide.properties");
        final String dockerArtifactAccount = config.getConfigData("dockerArtifactAccount");
        final String dockerID = config.getConfigData("dockerID");
        final String dockerConfigName = config.getConfigData("dockerConfigName");
        final String dockerReferenceName = config.getConfigData("dockerReferenceName");

        Map<String, Object> manifestArtifact = new HashMap<>();
        manifestArtifact.put("artifactAccount", dockerArtifactAccount);
        manifestArtifact.put("id", dockerID);
        manifestArtifact.put("name", dockerConfigName);
        manifestArtifact.put("reference", dockerReferenceName);
        manifestArtifact.put("type", "github/file");
        manifestArtifact.put("version", "main");

        return manifestArtifact;
    }

    private Map<String, Object> generateDeleteManifestObject(final String k8sAccountName, final String k8sNamespace) {
        Map<String, Object> deleteManifestData = new HashMap<>();
        deleteManifestData.put("account", k8sAccountName);
        deleteManifestData.put("app", "docker");
        deleteManifestData.put("cloudProvider", "kubernetes");
        deleteManifestData.put("location", k8sNamespace);
        deleteManifestData.put("manifestName", "deployment simple-deploy");
        deleteManifestData.put("mode", "static");
        deleteManifestData.put("name", "Delete (Manifest)");

        Map<String, Boolean> options = Collections.singletonMap("cascading", true);
        deleteManifestData.put("options", options);

        deleteManifestData.put("type", "deleteManifest");

        return deleteManifestData;
    }
}
