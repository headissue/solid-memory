package com.headissue.domain;

import java.util.Objects;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;

@SuppressWarnings("unused")
public class AccessRule {
  private String fileName;
  private Integer ttlDays;
  private UtmParameters utmParameters;
  private String owner;

  public AccessRule() {}

  public AccessRule(String fileName, Integer ttlDays, UtmParameters utmParameters, String owner) {
    this.fileName = fileName;
    this.ttlDays = ttlDays;
    this.utmParameters = utmParameters;
    this.owner = owner;
  }

  public String getFileName() {
    return fileName;
  }

  public Integer getTtlDays() {
    return ttlDays;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public void setTtlDays(Integer ttlDays) {
    this.ttlDays = ttlDays;
  }

  public UtmParameters getUtmParameters() {
    return utmParameters;
  }

  public void setUtmParameters(UtmParameters utmParameters) {
    this.utmParameters = utmParameters;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AccessRule that = (AccessRule) o;

    if (!Objects.equals(fileName, that.fileName)) return false;
    if (!Objects.equals(ttlDays, that.ttlDays)) return false;
    if (!Objects.equals(utmParameters, that.utmParameters)) return false;
    return Objects.equals(owner, that.owner);
  }

  @Override
  public int hashCode() {
    int result = fileName != null ? fileName.hashCode() : 0;
    result = 31 * result + (ttlDays != null ? ttlDays.hashCode() : 0);
    result = 31 * result + (utmParameters != null ? utmParameters.hashCode() : 0);
    result = 31 * result + (owner != null ? owner.hashCode() : 0);
    return result;
  }

  public static class Representer extends org.yaml.snakeyaml.representer.Representer {
    public Representer(DumperOptions options) {
      super(options);
      this.addClassTag(AccessRule.class, Tag.MAP);
    }

    @Override
    protected NodeTuple representJavaBeanProperty(
        Object javaBean, Property property, Object propertyValue, Tag customTag) {
      // if value of property is null, ignore it.
      if (propertyValue == null) {
        return null;
      } else {
        return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
      }
    }
  }
}
