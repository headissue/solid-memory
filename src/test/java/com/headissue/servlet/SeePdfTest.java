package com.headissue.servlet;

import static com.headissue.servlet.SeePdfTest.Given.given;
import static org.mockito.Mockito.*;

import com.github.jknack.handlebars.Handlebars;
import com.headissue.domain.AccessRule;
import com.headissue.domain.UtmParameters;
import com.headissue.service.FormKeyService;
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
import java.util.ArrayList;
import java.util.List;
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
    request = mock(HttpServletRequest.class);
    requestDispatcher = mock(RequestDispatcher.class, RETURNS_DEEP_STUBS);
    response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
    accessReporter = mock(Logger.class, RETURNS_DEEP_STUBS);
    Handlebars handlebars = mock(Handlebars.class, RETURNS_DEEP_STUBS);
    FormKeyService formKeyService = mock(FormKeyService.class, RETURNS_DEEP_STUBS);
    when(formKeyService.isValid(anyString(), anyString())).thenReturn(true);
    sut =
        new SeePdfs(sharedTempDir.toFile(), handlebars, accessReporter, new Yaml(), formKeyService);
    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  void whereSendingEmailAllowsAndTracksAccess() throws ServletException, IOException {
    given(request).askingForAccessiblePdf().withAllPreconditionsMet();
    sut.doPost(request, response);
    verify(accessReporter)
        .info(
            "access: test.pdf; by: email@example.com; utm_content: test; consentTo: false;");
  }

  @Test
  void whereOptionalConsentToUpdateIsSend() throws ServletException, IOException {
    given(request).askingForAccessiblePdf().withAllPreconditionsMet().withOptionalConsent();
    sut.doPost(request, response);
    verify(accessReporter)
        .info(
            "access: test.pdf; by: email@example.com; utm_content: test; consentTo: true;");
  }

  @Test
  void whereNoFormKeyIsSubmitted() throws ServletException, IOException {
    given(request).askingForAccessiblePdf().withAllPreconditionsMet().butMissingFormKeys();
    sut.doPost(request, response);
    verify(response).sendError(400);
  }

  @Test
  void whereDownloadingIsTracked() throws ServletException, IOException {
    given(request).askingForPdfDownload().withAllPreconditionsMet();
    sut.doPost(request, response);
    verify(accessReporter).info("download: test.pdf; by: email@example.com; utm_content: test;");
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

  @SuppressWarnings("UnusedReturnValue")
  static class Given {
    private final HttpServletRequest request;
    private final List<Part> parts = new ArrayList<>();

    private Given(HttpServletRequest requestMock) throws ServletException, IOException {
      this.request = requestMock;
      when(request.getParts()).thenReturn(parts);
    }

    public static Given given(HttpServletRequest requestMock) throws ServletException, IOException {
      return new Given(requestMock);
    }

    public Given askingForAccessiblePdf() {
      when(request.getPathInfo()).thenReturn("/" + willGrantAccess);
      return this;
    }

    public Given withAllPreconditionsMet() throws ServletException, IOException {
      Part visitorPart = buildPart("visitor", "email@example.com");
      when(request.getPart("visitor")).thenReturn(visitorPart);
      parts.add(visitorPart);
      withValidFormKeys();
      return this;
    }

    private Given withValidFormKeys() throws IOException, ServletException {
      Part formKeyPart = buildPart("key", "123123123");
      when(request.getPart("key")).thenReturn(formKeyPart);
      Part formKeyHashPart = buildPart("hash", "123123123");
      when(request.getPart("hash")).thenReturn(formKeyHashPart);
      parts.add(formKeyPart);
      parts.add(formKeyHashPart);
      return this;
    }

    public Given withOptionalConsent() throws IOException, ServletException {
      Part consentPart = buildPart("consentTo", "true");
      when(request.getPart("consentTo")).thenReturn(consentPart);
      parts.add(consentPart);
      return this;
    }

    public Given butMissingFormKeys() throws ServletException, IOException {
      when(request.getPart("key")).thenReturn(null);
      when(request.getPart("hash")).thenReturn(null);
      parts.removeIf(part -> part.getName().equals("key") || part.getName().equals("hash"));
      return this;
    }

    public Given askingForPdfDownload() {
      when(request.getPathInfo()).thenReturn("/" + downloadable + "/download");
      return this;
    }
    private static Part buildPart(String key, String value) throws IOException {
      Part part = mock(Part.class, RETURNS_DEEP_STUBS);
      when(part.getName()).thenReturn(key);
      when(part.getInputStream()).thenReturn(new ByteArrayInputStream(value.getBytes()));
      return part;
    }
  }
}
