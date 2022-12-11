package com.headissue.domain;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;

import com.headissue.Application;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AccessRuleTest {

  @TempDir static Path sharedTempDir;

  @Test
  void whereWritingAndReadingAccessRuleEquals() throws FileNotFoundException {
    AccessRule accessRule = new AccessRule("test.pdf", 365 * 100);
    File file = sharedTempDir.resolve("equals.yaml").toFile();
    try (PrintWriter p = new PrintWriter(new FileOutputStream(file))) {
      Application.yaml.dump(accessRule, p);
    }
    assertThat(
        accessRule,
        CoreMatchers.is(Application.yaml.loadAs(new FileInputStream(file), AccessRule.class)));
  }

  @Test
  void whereTtlIsOptionalAndNotWrittenToFile() throws IOException {
    AccessRule accessRule = new AccessRule("test.pdf", null);
    File file = sharedTempDir.resolve("optional.yaml").toFile();
    try (PrintWriter p = new PrintWriter(new FileOutputStream(file))) {
      Application.yaml.dump(accessRule, p);
    }
    assertThat(
        accessRule,
        CoreMatchers.is(Application.yaml.loadAs(new FileInputStream(file), AccessRule.class)));
    try (Stream<String> lines = Files.lines(file.toPath())) {
      assertThat(lines.collect(Collectors.joining()), not(containsString("ttlDays")));
    }
  }
}
