package com.example.extractor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class PackageLister {

    private final String bucket = "ci-cd-config-store-demo-us";

    public String listAllPackages() throws Exception {
        Process list = Runtime.getRuntime().exec(new String[]{
                "aws", "s3", "ls", "s3://" + bucket + "/deployment-packages/"
        });
        list.waitFor();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(list.getInputStream()))) {
            return reader.lines()
                    .filter(line -> line.contains(".zip"))
                    .map(String::trim)
                    .collect(Collectors.joining("\n"));
        }
    }
}

