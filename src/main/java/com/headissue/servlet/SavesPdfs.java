package com.headissue.servlet;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.headissue.config.NanoIdConfig;
import com.headissue.domain.AccessRule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import org.eclipse.jetty.http.MimeTypes;
import org.yaml.snakeyaml.Yaml;

public class SavesPdfs extends HttpServlet {

  private final File directory;
  private final Yaml yaml;
  private final Random random = new Random();

  public SavesPdfs(File directory, Yaml yaml) {
    this.directory = directory;
    this.yaml = yaml;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    resp.getWriter()
        .print(
            "\n"
                + "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <title>Title</title>\n"
                + "</head>\n"
                + "<body style=\"background: darkgray; display: flex; margin: 1em; justify-content: center;\"\n"
                + "      onload=\"document.getElementById('id-form').style.opacity=1\">\n"
                + "<div id=\"id-form\" class=\"container\" style=\"display: block; max-width: 600px;border: 1px solid aliceblue; padding: 1em; border-radius: 10px; background: white; box-shadow: 0 1px  20px 10px #808080;-webkit-transition: opacity 0.2s 0.2s ease;\n"
                + "-moz-transition: opacity 0.2s 0.2s ease;\n"
                + "-o-transition: opacity 0.2s 0.2s ease;\n"
                + "transition: opacity 0.2s 0.2s ease; opacity: 0;\">\n"
                + "    \n"
                + "    <form enctype=\"multipart/form-data\" method=\"post\" onsubmit=\"function setAction(form) {\n"
                + "        form.action = window.location.href\n"
                + "        return form.submit;\n"
                + "    }\n"
                + "    return setAction(this)\">\n"
                + "        <label>\n"
                + "            file upload:\n"
                + "            <input type=\"file\" name=\"file\">\n"
                + "        </label><br>\n"
                + "        <label>\n"
                + "            access expires in (with 0 meaning never expires):\n"
                + "            <input type=\"number\" value=\"0\" min=\"0\" name=\"ttlDays\">\n"
                + "        </label>\n"
                + "        <button type=\"submit\">SEND</button>\n"
                + "    </form>\n"
                + "</div>\n"
                + "</body>\n"
                + "</html>");
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Path directoryPath = Paths.get(directory.getPath());
    Part filePart =
        req.getParts().stream().filter(it -> it.getName().equals("file")).findFirst().orElseThrow();
    String fileName =
        String.format(
            "%s_%s.pdf",
            filePart.getSubmittedFileName().replaceAll(".pdf", ""), Instant.now().getEpochSecond());
    filePart.write(directoryPath.resolve(fileName).toString());
    Optional<Part> ttlDaysPart =
        req.getParts().stream().filter(it -> it.getName().equals("ttlDays")).findFirst();
    int ttlDays = 0;
    if (ttlDaysPart.isPresent()) {
      ttlDays =
          Integer.parseInt(
              new String(
                  ttlDaysPart.get().getInputStream().readAllBytes(), StandardCharsets.UTF_8));
    }
    AccessRule accessRule = new AccessRule(fileName, ttlDays == 0 ? null : ttlDays, null);
    String randomNanoId =
        NanoIdUtils.randomNanoId(random, NanoIdConfig.alphabet, NanoIdConfig.length);
    try (PrintWriter p =
        new PrintWriter(
            new FileOutputStream(directoryPath.resolve(randomNanoId + ".yaml").toString()))) {
      yaml.dump(accessRule, p);
    }
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    PrintWriter writer = resp.getWriter();
    String location = "/docs/" + randomNanoId;
    writer.print(
        "<!DOCTYPE html>\n"
            + "<html lang=\"en\">\n"
            + "<head>\n"
            + "    <meta charset=\"UTF-8\">\n"
            + "    <title>Title</title>\n"
            + "</head>\n"
            + "<body style=\"background: darkgray; display: flex; margin: 1em; justify-content: center;\">\n"
            + "<div id=\"id-form\" class=\"container\"\n"
            + "     style=\"display: block; max-width: 600px;border: 1px solid aliceblue; padding: 1em; border-radius: 10px; background: white; box-shadow: 0 1px  20px 10px #808080;\">\n"
            + "    ✅ the file is accessible\n"
            + "    <a id=\"access\" href=\""
            + location
            + "\">with this link</a>\n"
            + "</div>\n"
            + "</script>\n"
            + "</body>\n"
            + "</html>");
  }
}
