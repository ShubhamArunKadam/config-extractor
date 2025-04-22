package com.example.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RollbackService {

    private final String bucket = "ci-cd-config-store-demo-us";

    public File getPreviousDeploymentPackage() throws Exception {
        // List all deployments
        Process list = Runtime.getRuntime().exec(new String[]{
                "aws", "s3", "ls", "s3://" + bucket + "/deployment-packages/"
        });
        list.waitFor();

        InputStream in = list.getInputStream();
        List<String> lines = new java.util.Scanner(in).useDelimiter("\\A").nextLines().lines().toList();

        List<String> zips = lines.stream()
                .map(String::trim)
                .filter(line -> line.endsWith(".zip"))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (zips.size() < 2) throw new RuntimeException("Not enough deployments for rollback");

        String previousZip = zips.get(1);
        String zipName = previousZip.substring(previousZip.lastIndexOf(" ") + 1);

        File outFile = new File("/tmp/" + zipName);
        Process download = Runtime.getRuntime().exec(new String[]{
                "aws", "s3", "cp",
                "s3://" + bucket + "/deployment-packages/" + zipName,
                outFile.getAbsolutePath()
        });
        download.waitFor();

        return outFile;
    }
}

