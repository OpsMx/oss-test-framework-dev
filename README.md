# OSS Test Framework User Guide

## Introduction
The OSS Test Automation Framework is designed to automate the process of building/installing and testing a Spinnaker instance. The primary goal is to establish an automated and repeatable process for different environments. 

### The framework comprises four major modules:
1. **Build and Configure OSS:** This is achieved through Github actions:
   - Provision EKS cluster (if required)
   - Deploy Spinnaker
2. **Test Automation framework:** This is developed using Java. This is an easily extensible framework. The Test Framework must be compiled into a Docker image and deployed using Github actions as a K8s job to be run in the same namespace where OSS is installed.  Please see the "User Guide" under Section “How to compile Test Framework into a docker image”.
3. **Execute Test Suite and Report Test Results:**  This is achieved through Github action:
   - Run Test Cases
4. **Clean up Infra:**  This is achieved through Github actions:
   - Destroy Infrastructure
   - Destroy Spinnaker

## Prerequisites
1. **If provisioning the EKS K8s cluster is required:**
     - Create Github secrets AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY and AWS_REGION  with appropriate permissions for the access Id.
     - Create a Github secret AWS_BUCKET with the AWS S3 bucket name where terraform state files are stored during K8s cluster creation and deletion.
2. **If the K8s cluster is pre-defined (i.e., not created in the step-1):**
     - Store kubeconfig file in AWS Secret Manager
3. Store values.yaml in AWS Secret Manager to be used by spinnaker installer (sample values.yaml is given in this repo)
4. Store config-overide.properties in the AWS Secret Manager which has the required configurations to be overridden.  The default config.properties is part of oss-test-framework image. (sample config-overide.properties and config.properities are given in this repo).
5. Create and store the halconfig that has all the Spinnaker configurations (cloud accounts, integrations etc) in AWS S3 bucket.  The AWS S3 bucket that has the halconfig must be given in values.yml.
    
## Execute Test Suite & Check Test Results
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Run the Github action “**Provision EKS Cluster**” if the creation of the EKS cluster is required (see the Section below: Creating a cluster)
3. Run the Github action “**Deploy Spinnaker**” (see the Section below: Deploy Spinnaker)
4. Configure the Test Suite (See How to configure the Test Suite section in **OSS Test Automation Framework UserGuide** which is in the repo.)
5. Run the Github action “**Run Test Cases**”  (see the Section below: Running Test Cases)
6. Run the Github action “**Destroy Spinnaker**” (see the Section below: Destroy the Spinnaker)
7. Run the Github action “**Destroy EKS cluster**” (see the Section below: Destroy the Infrastructure)
8. The Test Results are pushed to S3 and also available as a HTML link in Github actions.

## Creating a Cluster
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Navigate to the "**Actions**" tab.
3. Choose “**Provision EKS Cluster**
4. Click on the “**Run workflow**" button on the right side.
5. Fill in the appropriate details:
   - **EKS cluster name in AWS:** Specify the name of the EKS cluster in AWS to be created 
   - **Kubeconfig Secret Name in AWS:** Specify the name of Kubeconfig secret (AWS secret will be created with this name).
   - **KMS_KEY alias name:** Enter the alias  name of the AWS Key Management Service (KMS) key/Encryption key.
6. Click on the "**Run workflow**" button.
7. Monitor the running workflow to check its status.

## Deploy Spinnaker
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Navigate to the "**Actions**" tab.
3. Choose "**Deploy Spinnaker**".
4. Click on the "**Run workflow**" button.
5. Fill in the appropriate details:
   - **Secret name of kubeconfig:** Provide the AWS secret name which has cluster  kubeconfig.
   - **Secret name for values file:** Specify the AWS secret name which has values.yml file.
   - **Spinnaker namespace:** Provide the namespace for deploying Spinnaker (if not exist it will create a namespace).
   - **Helm Release:** Specify the release name for Spinnaker.
6. Click on the "**Run workflow**" button.
7. Check the status of the workflow by clicking on the running workflow and then OpsMx-Spinnaker. Spinnaker will be up and running once all checks are completed.

## Running Test Cases
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Navigate to the "**Actions**" tab.
3. Choose "**Run Test Cases**".
4. Click on the "**Run workflow**" button.
5. Fill in the appropriate details:
   - **Secret name of kubeconfig:** Specify the AWS secret name which has the kubeconfig stored in it.
   - **Namespace where the testcases should run:** Provide the namespace in which our test cases should run.
   - **Regular expression to run the test cases:** Regular expressions can be used to run specific test cases which are present in CommonTests.java.<br>
     Below are the sample expressions:
     - Empty space to execute all the test cases
     - `createApp\*`
     - `\*App\*`
     - `\*Pipe\*`
     - `createApp\*,\*Pipe\*`
     - `createApp\*,\*Pipe\*,\*Jenkin\*`
     - `createApp\*,\*Helm\*`

 7. Click on the "**Run workflow**" button.
 8. Monitor the running workflow to check its status.

### Test Case Report and Log Report:
Test case report,log reports for successful **Run Test Cases** github action can be viewed by following below steps:
1. Click on **Actions**
2. Select **Run Test Cases** github action
3. Select the Run Test Cases workflow run(ex: Run Test Cases #110) for which we need to view the reports,
   under **Test-Cases summary** section we can see the URLs of the Reports
    - TestCases Report
    - TestCases Logs
4. Click on  **TestCases Report** to view TestCases Report html file.
5. Click on  **TestCases Logs** to download the Log Report and view.

## Destroying the Spinnaker
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Navigate to the "**Actions**" tab.
3. Choose "**Destroy Spinnaker**"
4. Click on the "**Run workflow**" button.
5. Fill in the appropriate details:
   - **Secret name of kubeconfig:** Specify the AWS secret name where the kubeconfig is stored.
   - **Spinnaker namespace:** Provide the namespace for destroying Spinnaker.
   - **Helm Release:** Provide the release name for destroying Spinnaker
6. Click on the "**Run workflow**" button.
7. Monitor the running workflow to check its status.

## Destroying the Cluster
1. Navigate to the Git repository where GitHub Actions are configured (e.g.,https://github.com/OpsMx/oss-test-automation-framework).
2. Navigate to the "**Actions**" tab.
3. Choose "**Destroy EKS cluster**".
4. Click on the "**Run workflow**" button.
5. Fill in the appropriate details:
   - **Secret name of kubeconfig:** Provide the secret name (in AWS secret manager) containing the cluster kubeconfig to delete the cluster.
   - **EKS cluster name in AWS:** Provide the EKS cluster name in AWS for destroying the cluster
   - **KMS_KEY will be used to delete alias for KMS_KEY in AWS:** Provide Encryption key of the kubeconfig secret to destroy the cluster.  
6. Click on the "**Run workflow**" button.
7. Monitor the running workflow to check its status.

## Configuration Details
For detailed information on configuring the test suite refer to **OSS Test Automation Framework UserGuide** which is in the repo.  

## Sample Configurations
Sample config.properties, config-overide.properties, and values.yaml files are provided for reference in the document **OSS Test Automation Framework UserGuide**. 

## List of Automation Test Cases
For details on various test scenarios, please refer to the List of Automation Test Cases section in the document **OSS Test Automation Framework UserGuide**. 
