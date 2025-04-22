package com.example.extractor.controller;

import com.example.extractor.PackageBuilder;
import com.example.extractor.PackageMetadata;
import com.example.extractor.PackageUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
public class PackageController {

    @Autowired
    private PackageBuilder packageBuilder;

    @Autowired
    private PackageUploader packageUploader;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/package/create")
    public String createPackage(@RequestBody List<String> selectedSnapshots) {
        try {
            // ✅ Generate a unique name
            String packageName = "deployment-" + Instant.now().toString().replace(":", "-") + ".zip";
            String metadataName = packageName.replace(".zip", ".json");

            // ✅ Create ZIP from snapshots
            File zipFile = packageBuilder.createDeploymentPackage(selectedSnapshots, packageName);

            // ✅ Upload ZIP to S3
            packageUploader.uploadPackage(zipFile, packageName);

            // ✅ Generate metadata
            PackageMetadata metadata = new PackageMetadata(
                    packageName,
                    Instant.now().toString(),
                    "admin",  // you can replace this with real user from auth
                    selectedSnapshots,
                    "N/A"
            );

            // ✅ Save metadata to temporary JSON file
            File metaFile = File.createTempFile("metadata-", ".json");
            objectMapper.writeValue(metaFile, metadata);

            // ✅ Upload metadata JSON to S3
            packageUploader.uploadPackage(metaFile, metadataName);

            return "✅ Deployment package + metadata uploaded: " + packageName;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Failed: " + e.getMessage();
        }
    }
}
