package com.headissue.domain;

public class AccessRule {
    private String fileName;
    private long ttlSeconds;

    public AccessRule() {
    }

    public AccessRule(String fileName, long ttlSeconds) {
        this.fileName = fileName;
        this.ttlSeconds = ttlSeconds;
    }

    public String getFileName() {
        return fileName;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }
}
