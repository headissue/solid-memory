package com.headissue.service;

import java.time.Instant;
import java.util.Random;

public class FormKeyService {

  private static final Random random = new Random();

  private static String secret = String.valueOf(random.nextInt());

  public FormKey getFormKey() {
    String key =
        String.valueOf(
            String.valueOf((Instant.now().getEpochSecond() - Instant.now().getNano())).hashCode());
    return new FormKey(key);
  }

  public boolean isValid(String hash, String key) {
    return new FormKey(key).getHash().equals(hash);
  }

  static class FormKey {
    private String hash;
    private String key;

    public FormKey(String key) {
      this.hash = String.valueOf((key + secret).hashCode());
      this.key = key;
    }

    public String getHash() {
      return hash;
    }

    public String getKey() {
      return key;
    }
  }
}
