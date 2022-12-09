package com.headissue.domain;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;

@SuppressWarnings("unused")
public class AccessRule {
  private String fileName;
  private long ttlSeconds;

  public AccessRule() {}

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

  public static class Representer extends org.yaml.snakeyaml.representer.Representer {
    public Representer(DumperOptions options) {
      super(options);
      this.addClassTag(AccessRule.class, Tag.MAP);
    }
  }
}
