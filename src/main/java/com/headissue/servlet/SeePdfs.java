package com.headissue.servlet;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.internal.lang3.StringUtils;
import com.headissue.config.NanoIdConfig;
import com.headissue.domain.AccessRule;
import com.headissue.domain.UtmParameters;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.*;
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

public class SeePdfs extends HttpServlet {

  private static final Logger logger = LoggerFactory.getLogger(SeePdfs.class);

  private final Handlebars handlebars;
  private final File directory;

  private final Logger pdfLogger;
  private final Yaml yaml;

  public SeePdfs(File directory, Handlebars handlebars, Logger pdfLogger, Yaml yaml) {
    this.directory = directory;
    this.handlebars = handlebars;
    this.pdfLogger = pdfLogger;
    this.yaml = yaml;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    String pathInfo = req.getPathInfo();
    String accessId = pathInfo.substring("/".length());
    Path accessYaml = Paths.get(directory.getPath(), Path.of(accessId).getFileName() + ".yaml");
    if (Files.notExists(accessYaml)) {
      throw new NoSuchFileException(accessId);
    }
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    AccessRule accessRule = yaml.loadAs(new FileInputStream(accessYaml.toFile()), AccessRule.class);
    if (isExpired(accessYaml)) {
      resp.setStatus(HttpServletResponse.SC_GONE);
      handlebars
          .compile("docs/expired.hbs")
          .apply(Map.of("accessRule", accessRule), resp.getWriter());
      return;
    }
    Path pdfPath = Paths.get(directory.getPath(), accessRule.getFileName());
    byte[] bytes = Files.readAllBytes(pdfPath);
    byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
    String base64Pdf = new String(encoded);

    handlebars
        .compile("docs/preview.hbs")
        .apply(
            Map.of("id", accessId, "accessRule", accessRule, "base64Pdf", base64Pdf),
            resp.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String pathInfo = req.getPathInfo();
    String accessId =
        StringUtils.left(req.getPathInfo().substring("/".length()), NanoIdConfig.length);
    Path accessYaml = Paths.get(directory.getPath(), Path.of(accessId).getFileName() + ".yaml");
    checkExistenceAndExpiry(accessId, accessYaml);
    AccessRule accessRule = yaml.loadAs(new FileInputStream(accessYaml.toFile()), AccessRule.class);
    Path pdfPath = Paths.get(directory.getPath(), accessRule.getFileName());

    String key = "visitor";
    boolean noVisitorPart = isMissingRequiredPart(req, key);
    if (noVisitorPart) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    String visitor = new String(req.getPart(key).getInputStream().readAllBytes(), UTF_8);

    if (pathInfo.matches(format(".*.{%d}/download$", NanoIdConfig.length))) {
      if (!accessRule.isPermitDownload()) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return;
      }

      reportDownload(accessRule, visitor);

      byte[] buffer = new byte[1024];
      try (InputStream in = Files.newInputStream(pdfPath)) {
        OutputStream output = resp.getOutputStream();
        for (int length; (length = in.read(buffer)) > 0; ) {
          output.write(buffer, 0, length);
        }
      }
      return;
    }
    Part consentToMonthlyUpdatesPart = req.getPart("consentToMonthlyUpdates");
    Boolean consentToMonthlyUpdates =
        consentToMonthlyUpdatesPart != null
            && Boolean.parseBoolean(
                new String(consentToMonthlyUpdatesPart.getInputStream().readAllBytes(), UTF_8));

    report(accessRule, visitor, "access: ", consentToMonthlyUpdates);

    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    byte[] bytes = Files.readAllBytes(pdfPath);
    byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
    String base64Pdf = new String(encoded);

    handlebars
        .compile("docs/showDoc.hbs")
        .apply(
            Map.of(
                "base64Pdf",
                base64Pdf,
                "accessRule",
                accessRule,
                "id",
                accessId,
                "visitor",
                visitor),
            resp.getWriter());
  }

  private static boolean isMissingRequiredPart(HttpServletRequest req, String key) {
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

  private void reportDownload(AccessRule accessRule, String visitor) {
    report(accessRule, visitor, "download: ", null);
  }

  private void report(
      AccessRule accessRule, String visitor, String type, Boolean consentToMonthlyUpdates) {
    StringBuilder sb = new StringBuilder();
    sb.append(type).append(accessRule.getFileName()).append("; ");
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
    if (consentToMonthlyUpdates != null) {
      sb.append("consentToMonthlyUpdates: ").append(consentToMonthlyUpdates).append("; ");
    }
    pdfLogger.info(sb.toString().trim());
  }

  private void checkExistenceAndExpiry(String accessId, Path accessYaml) throws IOException {
    if (Files.notExists(accessYaml)) {
      throw new NoSuchFileException(accessId);
    }
    if (isExpired(accessYaml)) {
      throw new NoSuchFileException(accessId);
    }
  }
}
