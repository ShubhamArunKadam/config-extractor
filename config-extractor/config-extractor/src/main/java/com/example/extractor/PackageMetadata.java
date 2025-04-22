package com.example.extractor;

import java.util.List;

public class PackageMetadata {
    private String packageName;
    private String createdAt;
    private String createdBy;
    private List<String> includedSnapshots;
    private String targetAccountId; // optional for Phase 3

    public PackageMetadata(String packageName, String createdAt, String createdBy, List<String> includedSnapshots, String targetAccountId) {
        this.packageName = packageName;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.includedSnapshots = includedSnapshots;
        this.targetAccountId = targetAccountId;
    }

    // ✅ Add getters and setters if needed by Jackson
    public String getPackageName() { return packageName; }
    public String getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public List<String> getIncludedSnapshots() { return includedSnapshots; }
    public String getTargetAccountId() { return targetAccountId; }

    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setIncludedSnapshots(List<String> includedSnapshots) { this.includedSnapshots = includedSnapshots; }
    public void setTargetAccountId(String targetAccountId) { this.targetAccountId = targetAccountId; }
}
