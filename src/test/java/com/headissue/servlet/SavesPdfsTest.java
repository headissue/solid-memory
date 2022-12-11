package com.headissue.servlet;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.Yaml;

class SavesPdfsTest {

  @TempDir static Path sharedTempDir;

  SavesPdfs sut;

  HttpServletRequest request;
  HttpServletResponse response;

  @BeforeAll
  static void setUpClass() {}

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
    response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
    sut = new SavesPdfs(sharedTempDir.toFile(), new Yaml());

    ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
    ServletRegistration.Dynamic savePdf =
        servletContextHandler.getServletContext().addServlet("savePdf", sut);
    savePdf.setLoadOnStartup(1);
    savePdf.addMapping("/public/share");
    savePdf.setMultipartConfig(
        new MultipartConfigElement(
            sharedTempDir.toString(), 1024 * 1024 * 10, 1024 * 1024 * 10, 0));
  }

  @Test
  void whereSendingMultipartPostWritesPartToFileAndWritesAccessRuleFile()
      throws IOException, ServletException {
    Part filePart = mock(Part.class, RETURNS_DEEP_STUBS);
    when(filePart.getSubmittedFileName()).thenReturn("sample.pdf");
    when(filePart.getName()).thenReturn("file");
    Part ttlPart = mock(Part.class, RETURNS_DEEP_STUBS);
    when(ttlPart.getName()).thenReturn("ttlDays");
    when(ttlPart.getInputStream()).thenReturn(new ByteArrayInputStream("7".getBytes()));
    when(request.getParts()).thenReturn(List.of(filePart, ttlPart));
    sut.doPost(request, response);
    verify(filePart, times(1)).write(anyString());
    try (Stream<Path> list = Files.list(sharedTempDir)) {
      assertThat(
          "access rule yaml was written to temp dir",
          list.anyMatch(it -> it.getFileName().toString().endsWith(".yaml")));
    }
  }
}
