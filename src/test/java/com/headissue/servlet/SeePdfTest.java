package com.headissue.servlet;

import static org.mockito.Mockito.*;

import com.github.jknack.handlebars.Handlebars;
import com.headissue.domain.AccessRule;
import com.headissue.domain.UtmParameters;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

class SeePdfTest {

  @TempDir static Path sharedTempDir;

  Logger accessReporter;

  SeePdfs sut;

  HttpServletRequest request;
  HttpServletResponse response;
  RequestDispatcher requestDispatcher;

  static String willGrantAccess = "00000000";
  static String downloadable = "00000001";
  static String notDownloadable = "00000002";

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
    Handlebars handlebars = mock(Handlebars.class, RETURNS_DEEP_STUBS);
    sut = new SeePdfs(sharedTempDir.toFile(), handlebars, accessReporter, new Yaml());
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  void whereSendingEmailAllowsAndTracksAccess() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/" + willGrantAccess);
    Part part = mock(Part.class, RETURNS_DEEP_STUBS);
    when(part.getName()).thenReturn("accessor");
    when(part.getInputStream())
        .thenReturn(new ByteArrayInputStream("email@example.com".getBytes()));
    when(request.getPart("accessor")).thenReturn(part);
    sut.doPost(request, response);
    verify(accessReporter).info("access: test.pdf; by: email@example.com; utm_content: test;");
  }

  @Test
  void whereDownloadingIsTracked() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/" + notDownloadable + "/download");
    Part part = mock(Part.class, RETURNS_DEEP_STUBS);
    when(part.getName()).thenReturn("accessor");
    when(part.getInputStream())
        .thenReturn(new ByteArrayInputStream("email@example.com".getBytes()));
    when(request.getPart("accessor")).thenReturn(part);
    sut.doPost(request, response);
    verify(response).sendError(400);
  }

  @Test
  void whereDownloadingIsNotPermitted() throws ServletException, IOException {
    when(request.getPathInfo()).thenReturn("/" + willGrantAccess);
    Part part = mock(Part.class, RETURNS_DEEP_STUBS);
    when(part.getName()).thenReturn("accessor");
    when(part.getInputStream())
        .thenReturn(new ByteArrayInputStream("email@example.com".getBytes()));
    when(request.getPart("accessor")).thenReturn(part);
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
                new FileOutputStream(sharedTempDir.resolve(willGrantAccess + ".yaml").toFile()));
        PrintWriter downloadableWriter =
            new PrintWriter(
                new FileOutputStream(sharedTempDir.resolve(downloadable + ".yaml").toFile()));
        PrintWriter notDownloadableWriter =
            new PrintWriter(
                new FileOutputStream(sharedTempDir.resolve(notDownloadable + ".yaml").toFile()))) {
      yaml.dump(
          new AccessRule(
              "test.pdf",
              1,
              new UtmParameters(null, null, null, null, "test"),
              "yours truly",
              false),
          accessWriter);
      yaml.dump(
          new AccessRule(
              "test.pdf", 1, new UtmParameters(null, null, null, null, "test"), null, true),
          downloadableWriter);
      yaml.dump(new AccessRule("test.pdf", 1, null, null, false), notDownloadableWriter);
    }
    Files.createFile(sharedTempDir.resolve("test.pdf"));
  }
}
