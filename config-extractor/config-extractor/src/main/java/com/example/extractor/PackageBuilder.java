package com.example.extractor;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class PackageBuilder {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
            .withRegion("us-east-1")
            .build();

    private final String bucketName = "ci-cd-config-store-demo-us";

    public File createDeploymentPackage(List<String> s3Keys, String outputFileName) throws IOException {
        File zipFile = new File("/tmp/" + outputFileName);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String s3Key : s3Keys) {
                File tempFile = File.createTempFile("snapshot-", ".json");
                s3Client.getObject(bucketName, s3Key).getObjectContent().transferTo(new FileOutputStream(tempFile));

                try (FileInputStream fis = new FileInputStream(tempFile)) {
                    ZipEntry entry = new ZipEntry(s3Key.replace("configs/", ""));
                    zos.putNextEntry(entry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    zos.closeEntry();
                }

                tempFile.delete(); // Clean up
            }
        }

        System.out.println("✅ ZIP created at: " + zipFile.getAbsolutePath());
        return zipFile;
    }
}
