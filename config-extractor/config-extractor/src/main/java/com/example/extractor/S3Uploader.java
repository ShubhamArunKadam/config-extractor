package com.example.extractor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class S3Uploader {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final String bucketName = "ci-cd-config-store-demo-us"; // 🔁 Change if needed

    public void uploadJson(Map<String, Object> data, String key) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            s3Client.putObject(bucketName, key, json);
            System.out.println("✅ Uploaded to S3: " + key);
        } catch (Exception e) {           System.err.println("❌ Upload failed: " + e.getMessage());
        }
    }
}
