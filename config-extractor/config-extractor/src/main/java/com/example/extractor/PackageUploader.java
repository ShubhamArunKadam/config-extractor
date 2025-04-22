package com.example.extractor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class PackageUploader {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion("us-east-1")
            .build();

    private final String bucketName = "ci-cd-config-store-demo-us";

    public void uploadPackage(File zipFile, String packageName) {
        String s3Key = "deployment-packages/" + packageName;

        s3Client.putObject(bucketName, s3Key, zipFile);
        System.out.println("✅ Package uploaded to S3: " + s3Key);
    }
}
