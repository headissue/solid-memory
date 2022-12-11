package com.headissue.domain;

import java.util.Objects;

@SuppressWarnings("unused")
public class UtmParameters {

  // referrer e.g. google, newsletter
  private String source;
  //  what type of link was used, such as cost per click or email.
  private String medium;
  // product, promo code or slogen e.g. "spring_sale"
  private String campaign;
  // search keywords
  private String term;
  // what exactly was clicked e.g. banner,
  private String content;

  public UtmParameters() {}

  public UtmParameters(String source, String medium, String campaign, String term, String content) {
    this.source = source;
    this.medium = medium;
    this.campaign = campaign;
    this.term = term;
    this.content = content;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getMedium() {
    return medium;
  }

  public void setMedium(String medium) {
    this.medium = medium;
  }

  public String getCampaign() {
    return campaign;
  }

  public void setCampaign(String campaign) {
    this.campaign = campaign;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UtmParameters that = (UtmParameters) o;

    if (!Objects.equals(source, that.source)) return false;
    if (!Objects.equals(medium, that.medium)) return false;
    if (!Objects.equals(campaign, that.campaign)) return false;
    if (!Objects.equals(term, that.term)) return false;
    return Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    int result = source != null ? source.hashCode() : 0;
    result = 31 * result + (medium != null ? medium.hashCode() : 0);
    result = 31 * result + (campaign != null ? campaign.hashCode() : 0);
    result = 31 * result + (term != null ? term.hashCode() : 0);
    result = 31 * result + (content != null ? content.hashCode() : 0);
    return result;
  }
}
