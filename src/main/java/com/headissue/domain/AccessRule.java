package com.headissue.domain;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Tag;

@SuppressWarnings("unused")
public class AccessRule {
  private String fileName;
  private long ttlDays;

  public AccessRule() {}

  public AccessRule(String fileName, long ttlDays) {
    this.fileName = fileName;
    this.ttlDays = ttlDays;
  }

  public String getFileName() {
    return fileName;
  }

  public long getTtlDays() {
    return ttlDays;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setTtlDays(long ttlDays) {
    this.ttlDays = ttlDays;
  }

  public static class Representer extends org.yaml.snakeyaml.representer.Representer {
    public Representer(DumperOptions options) {
      super(options);
      this.addClassTag(AccessRule.class, Tag.MAP);
    }
  }
}
