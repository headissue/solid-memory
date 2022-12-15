package com.headissue.servlet;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.headissue.domain.AccessRule;
import com.headissue.domain.UtmParameters;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class TemplateRendering extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(TemplateRendering.class);
  private final File directory;
  private final Logger accessReporter;
  private final Yaml yaml;

  public TemplateRendering(File directory, Logger accessReporter, Yaml yaml) {
    this.directory = directory;
    this.accessReporter = accessReporter;
    this.yaml = yaml;
  }

  private static final Handlebars handlebars;

  static {
    handlebars = new Handlebars(new ClassPathTemplateLoader("/static", ""));
    StringHelpers stringFormat = StringHelpers.stringFormat;
    handlebars.registerHelper("format", stringFormat);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    String pathInfo = req.getPathInfo();
    if (pathInfo.equals("/")) {
      handlebars.compile("index.hbs").apply(null, resp.getWriter());
    } else if (pathInfo.startsWith("/docs/")) {
      handleGetDoc(resp, pathInfo);
    } else {
      handlebars.compile(pathInfo).apply(null, resp.getWriter());
    }
  }

  private void handleGetDoc(HttpServletResponse resp, String pathInfo) throws IOException {
    String accessId = pathInfo.substring("/".length());
    Path accessYaml = Paths.get(directory.getPath(), Path.of(accessId).getFileName() + ".yaml");
    checkExistenceAndExpiry(accessId, accessYaml);
    AccessRule accessRule = yaml.loadAs(new FileInputStream(accessYaml.toFile()), AccessRule.class);
    handlebars
        .compile("docs/preDocCaptureEmail.hbs")
        .apply(
            Map.of("id", pathInfo.substring("/docs/".length()), "accessRule", accessRule),
            resp.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String pathInfo = req.getPathInfo();
    if (!pathInfo.startsWith("/docs/")) {
      resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    String key = "id";
    boolean noParameter = isMissingRequiredParameter(req, key);
    if (noParameter) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }
    String accessId = req.getPathInfo().substring("/".length());
    Path accessYaml = Paths.get(directory.getPath(), Path.of(accessId).getFileName() + ".yaml");

    checkExistenceAndExpiry(accessId, accessYaml);

    AccessRule accessRule = yaml.loadAs(new FileInputStream(accessYaml.toFile()), AccessRule.class);
    String visitor =
        new String(req.getPart(key).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    reportAccess(accessRule, visitor);

    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    byte[] bytes = Files.readAllBytes(Paths.get(directory.getPath(), accessRule.getFileName()));
    byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
    String base64Pdf = new String(encoded);

    handlebars
        .compile("docs/showDoc.hbs")
        .apply(Map.of("base64Pdf", base64Pdf, "accessRule", accessRule), resp.getWriter());
  }

  private void checkExistenceAndExpiry(String accessId, Path accessYaml) throws IOException {
    if (Files.notExists(accessYaml)) {
      throw new NoSuchFileException(accessId);
    }
    if (isExpired(accessYaml)) {
      throw new NoSuchFileException(accessId);
    }
  }

  private void reportAccess(AccessRule accessRule, String visitor) {
    StringBuilder sb = new StringBuilder();
    sb.append("access: ").append(accessRule.getFileName()).append("; ");
    sb.append("by: ").append(visitor).append("; ");
    UtmParameters utmParameters = accessRule.getUtmParameters();
    if (utmParameters != null) {
      String content = utmParameters.getContent();
      if (content != null) {
        sb.append("utm_content: ").append(content).append("; ");
      }
      String source = utmParameters.getSource();
      if (source != null) {
        sb.append("utm_source: ").append(source).append("; ");
      }
      String medium = utmParameters.getMedium();
      if (medium != null) {
        sb.append("utm_medium: ").append(medium).append("; ");
      }
      String campaign = utmParameters.getCampaign();
      if (campaign != null) {
        sb.append("utm_campaign: ").append(campaign).append("; ");
      }
      String term = utmParameters.getTerm();
      if (term != null) {
        sb.append("utm_term: ").append(term).append("; ");
      }
    }
    accessReporter.info(sb.toString().trim());
  }

  private static boolean isMissingRequiredParameter(HttpServletRequest req, String key) {
    Collection<Part> parts;
    try {
      parts = req.getParts();
      if (parts.isEmpty()) {
        return true;
      }
      return req.getPart(key) == null;
    } catch (IOException e) {
      logger.error("exception getting Part", e);
      return true;
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isExpired(Path accessYaml) throws IOException {
    AccessRule accessRule = yaml.loadAs(new FileInputStream(accessYaml.toFile()), AccessRule.class);
    Integer ttlDays = accessRule.getTtlDays();
    if (ttlDays == null) {
      return false;
    }
    Path pdf = Paths.get(directory.getPath(), accessRule.getFileName());
    Instant now = Instant.now();
    BasicFileAttributes attributes = Files.readAttributes(pdf, BasicFileAttributes.class);
    FileTime fileTime = attributes.creationTime();

    Instant ttl = fileTime.toInstant().plus(ttlDays, ChronoUnit.DAYS);
    return ttl.isBefore(now);
  }
}
