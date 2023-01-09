package com.headissue.service;

import java.time.Instant;
import java.util.Random;

public class FormKeyService {

  private static final Random random = new Random();

  private static final String secret = String.valueOf(random.nextInt());

  public static final String FORM_KEY = "key";
  public static final String FORM_KEY_HASH = "hash";

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
    private final String hash;
    private final String key;

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
