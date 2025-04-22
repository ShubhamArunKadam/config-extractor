package com.example.extractor.controller;

import com.amazonaws.services.lambda.AWSLambda;
import com.example.extractor.DeployerService;
import com.example.extractor.LexV1DeploymentService; // 🔥 Added
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
public class DeployerController {

    private final String bucket = "ci-cd-config-store-demo-us";

    @Autowired
    private DeployerService deployerService;

    @Autowired // 🔥 Added
    private LexV1DeploymentService lexV1DeploymentService; // 🔥 Added

    @PostMapping("/deploy/package")
    public String deployToAccount(
            @RequestParam String packageName,
            @RequestParam String customerAccountId) {

        try {
            AWSLambda lambdaClient = deployerService.getClientForCustomerAccount(customerAccountId);

            File zipFile = new File("/tmp/" + packageName);
            Process p = Runtime.getRuntime().exec(
                    new String[]{"aws", "s3", "cp", "s3://" + bucket + "/deployment-packages/" + packageName, zipFile.getAbsolutePath()});
            p.waitFor();

            ObjectMapper mapper = new ObjectMapper();

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().startsWith("lambda/") && entry.getName().endsWith(".json")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        String json = baos.toString(StandardCharsets.UTF_8);
                        System.out.println("🛠️  Deploying Lambda config: " + entry.getName());
                        System.out.println("📄 Content: " + json);

                        JsonNode lambdaConfig = mapper.readTree(json);
                        deployerService.deployLambda(lambdaConfig, lambdaClient, customerAccountId);

                        zis.closeEntry();
                    }

                    // 🔥 Added Lex Config Deployment Logic
                    if (entry.getName().startsWith("lex/") && entry.getName().endsWith(".json")) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        String json = baos.toString(StandardCharsets.UTF_8);
                        System.out.println("🛠️  Deploying Lex config: " + entry.getName());
                        System.out.println("📄 Content: " + json);

                        JsonNode lexConfig = mapper.readTree(json);
                        lexV1DeploymentService.deployLex(lexConfig, deployerService.getCredentialsForCustomerAccount(customerAccountId)); // ✅ Changed

                        zis.closeEntry();
                    }
                }
            }

            return "✅ Deployment initiated for package: " + packageName + " to account: " + customerAccountId;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Deployment failed: " + e.getMessage();
        }
    }
}

