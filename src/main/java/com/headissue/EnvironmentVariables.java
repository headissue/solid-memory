package com.headissue;

import java.util.NoSuchElementException;
import java.util.Optional;

public class EnvironmentVariables {
  public static int getAsInt(String env, int defaultValue) {
    String value = System.getenv().get(env);
    if (value != null && !value.equals("")) {
      return Integer.parseInt(value);
    }
    return defaultValue;
  }

  public static String get(String env, String defaultValue) {
    return Optional.ofNullable(System.getenv(env)).orElse(defaultValue);
  }

  public static String get(String env) {
    return Optional.ofNullable(System.getenv(env))
        .orElseThrow(() -> new NoSuchElementException(env));
  }
}
