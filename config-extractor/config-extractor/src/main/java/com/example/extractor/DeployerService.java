package com.example.extractor;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

@Service
public class DeployerService {

    public AWSLambda getClientForCustomerAccount(String customerAccountId) {
        String roleArn = "arn:aws:iam::" + customerAccountId + ":role/ShubhzDeployerRole";

        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        AssumeRoleRequest req = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withExternalId("shubhz-cicd-demo")
                .withRoleSessionName("shubhz-deployment");

        AssumeRoleResult result = stsClient.assumeRole(req);

        BasicSessionCredentials tempCreds = new BasicSessionCredentials(
                result.getCredentials().getAccessKeyId(),
                result.getCredentials().getSecretAccessKey(),
                result.getCredentials().getSessionToken()
        );

        AWSLambda assumedClient = AWSLambdaClientBuilder.standard()
                .withRegion("us-east-1")
                .withCredentials(new AWSStaticCredentialsProvider(tempCreds))
                .build();

        System.out.println("✅ Successfully assumed role into customer account: " + customerAccountId);
        return assumedClient;
    }

    public void deployLambda(JsonNode lambdaConfig, AWSLambda client, String clientAccountId) throws Exception {
        String functionName = lambdaConfig.get("functionName").asText();
        String runtime = lambdaConfig.get("runtime").asText();
        String handler = lambdaConfig.get("handler").asText();
        int memory = lambdaConfig.get("memorySize").asInt();
        int timeout = lambdaConfig.get("timeout").asInt();

        ByteBuffer zipBuffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get("/tmp/lambda-payload.zip")));

        FunctionCode code = new FunctionCode().withZipFile(zipBuffer);

        CreateFunctionRequest createRequest = new CreateFunctionRequest()
            .withFunctionName(functionName)
            .withRuntime(runtime)
            .withHandler(handler)
            .withMemorySize(memory)
            .withTimeout(timeout)
            .withCode(code)
            .withRole("arn:aws:iam::" + clientAccountId + ":role/ShubhzLambdaExecutionRole");

        client.createFunction(createRequest);
        System.out.println("✅ Lambda created in client account: " + functionName);
    }

    // 🔥 Added this method
    public BasicSessionCredentials getCredentialsForCustomerAccount(String customerAccountId) {
        String roleArn = "arn:aws:iam::" + customerAccountId + ":role/ShubhzDeployerRole";

        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withRegion("us-east-1")
                .build();

        AssumeRoleRequest req = new AssumeRoleRequest()
                .withRoleArn(roleArn)
                .withExternalId("shubhz-cicd-demo")
                .withRoleSessionName("shubhz-deployment");

        AssumeRoleResult result = stsClient.assumeRole(req);

        return new BasicSessionCredentials(
                result.getCredentials().getAccessKeyId(),
                result.getCredentials().getSecretAccessKey(),
                result.getCredentials().getSessionToken()
        );
    }
}

