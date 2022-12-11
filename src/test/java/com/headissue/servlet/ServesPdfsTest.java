package com.headissue.servlet;

import static org.mockito.Mockito.*;

import com.headissue.domain.AccessRule;
import com.headissue.domain.UtmParameters;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

class ServesPdfsTest {

  @TempDir static Path sharedTempDir;

  Logger accessReporter;

  ServesPdfs sut;

  HttpServletRequest request;
  HttpServletResponse response;
  RequestDispatcher requestDispatcher;

  @BeforeAll
  static void setUpClass() throws IOException {
    writeMockAccessFiles();
  }

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
    requestDispatcher = mock(RequestDispatcher.class, RETURNS_DEEP_STUBS);
    response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
    accessReporter = mock(Logger.class, RETURNS_DEEP_STUBS);
    sut = new ServesPdfs(sharedTempDir.toFile(), accessReporter, new Yaml());

    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  void whereOpeningWrongLinkForwardsToNotFound() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/doesNotExist");
    sut.doGet(request, response);
    verify(request).getRequestDispatcher("/404.html");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  void whereOpeningExpiredLinkForwardsToNotFound() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/expired");
    sut.doGet(request, response);
    verify(request).getRequestDispatcher("/404.html");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  void whereOpeningLiveLinkWithoutIdParameterForwardsToForm() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/willGrantAccess");
    when(request.getParameterMap()).thenReturn(Collections.emptyMap());
    sut.doGet(request, response);
    verify(request).getRequestDispatcher("/public/idForm");
    verify(requestDispatcher).forward(request, response);
  }

  @Test
  void whereOpeningLiveLinkWithIdParameterReportAccess() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/willGrantAccess");
    Part part = mock(Part.class, RETURNS_DEEP_STUBS);
    when(part.getName()).thenReturn("id");
    when(part.getInputStream())
        .thenReturn(new ByteArrayInputStream("email@example.com".getBytes()));
    when(request.getPart("id")).thenReturn(part);
    sut.doPost(request, response);
    verify(accessReporter).info("access: test.pdf; by: email@example.com; utm_content: test;");
  }

  // TODO the whole filestore should be a client so we can mock it and switch underlying tech, mock
  // file generation times etc
  static void writeMockAccessFiles() throws IOException {
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml yaml = new Yaml(options);

    try (PrintWriter accessWriter =
            new PrintWriter(
                new FileOutputStream(sharedTempDir.resolve("willGrantAccess.yaml").toFile()));
        PrintWriter expiredWriter =
            new PrintWriter(new FileOutputStream(sharedTempDir.resolve("expired.yaml").toFile()))) {
      yaml.dump(
          new AccessRule("test.pdf", 1, new UtmParameters(null, null, null, null, "test")),
          accessWriter);
      yaml.dump(new AccessRule("test.pdf", -1, null), expiredWriter);
    }
    Files.createFile(sharedTempDir.resolve("test.pdf"));
  }
}
