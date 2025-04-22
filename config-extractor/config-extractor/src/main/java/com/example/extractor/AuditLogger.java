package com.example.extractor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;

public class AuditLogger {

    private static final String AUDIT_FILE = "/tmp/audit-log.txt";

    public static void log(String action, String accountId, String packageName) {
        try (FileWriter fw = new FileWriter(AUDIT_FILE, true)) {
            fw.write(Instant.now() + " - " + action + " - " + packageName + " - Account: " + accountId + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readLogs() throws IOException {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(AUDIT_FILE)));
    }
}

