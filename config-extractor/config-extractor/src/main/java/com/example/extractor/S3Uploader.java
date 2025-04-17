package com.example.extractor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class S3Uploader {
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final String bucketName = "ci-cd-config-store-demo"; // Replace if needed

    public void uploadJson(Map<String, Object> data, String key) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            s3Client.putObject(bucketName, key, json);
            System.out.println("Uploaded: " + key);
        } catch (Exception e) {
            System.err.println("S3 Upload Failed: " + e.getMessage());
        }
    }
}

