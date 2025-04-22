package com.example.extractor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SnapshotService {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion("us-east-1") // 🔁 match your bucket region
            .build();

    private final String bucketName = "ci-cd-config-store-demo-us";

    public List<String> listAllSnapshots() {
        List<String> snapshots = new ArrayList<>();

        // Lambda snapshots
        snapshots.addAll(listFromPrefix("configs/lambda/"));

        // Lex snapshots
        snapshots.addAll(listFromPrefix("configs/lex/"));

        return snapshots;
    }

    private List<String> listFromPrefix(String prefix) {
        List<String> files = new ArrayList<>();

        ListObjectsV2Request req = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix(prefix);
        for (S3ObjectSummary summary : s3Client.listObjectsV2(req).getObjectSummaries()) {
            files.add(summary.getKey());
        }

        return files;
    }
}
