package com.headissue.servlet;

import com.headissue.domain.AccessRule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.eclipse.jetty.http.MimeTypes;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class SavesPdfs extends HttpServlet {

    private final File directory;
    private final Yaml yaml = new Yaml();

    public SavesPdfs(File directory) {
        this.directory = directory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
        resp.getWriter().print(
                "\n" +
                        "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Title</title>\n" +
                        "</head>\n" +
                        "<body style=\"background: darkgray; display: flex; margin: 1em; justify-content: center;\"\n" +
                        "      onload=\"document.getElementById('id-form').style.opacity=1\">\n" +
                        "<div id=\"id-form\" class=\"container\" style=\"display: block; max-width: 600px;border: 1px solid aliceblue; padding: 1em; border-radius: 10px; background: white; box-shadow: 0 1px  20px 10px #808080;-webkit-transition: opacity 0.2s 0.2s ease;\n" +
                        "-moz-transition: opacity 0.2s 0.2s ease;\n" +
                        "-o-transition: opacity 0.2s 0.2s ease;\n" +
                        "transition: opacity 0.2s 0.2s ease; opacity: 0;\">\n" +
                        "    <p>Please enter your email to continue</p>\n" +
                        "    <form enctype=\"multipart/form-data\" method=\"post\" onsubmit=\"function setAction(form) {\n" +
                        "        form.action = window.location.href\n" +
                        "        return form.submit;\n" +
                        "    }\n" +
                        "    return setAction(this)\">\n" +
                        "        <label>\n" +
                        "            file upload:\n" +
                        "            <input type=\"file\" name=\"file\">\n" +
                        "        </label><br>\n" +
                        "        <label>\n" +
                        "            access expires in:\n" +
                        "            <select name=\"ttl\">\n" +
                        "                <option value=\"3600\">one hour</option>\n" +
                        "                <option value=\"86400\">one day</option>\n" +
                        "                <option value=\"604800\">one week</option>\n" +
                        "            </select>\n" +
                        "        </label>\n" +
                        "        <button type=\"submit\">SEND</button>\n" +
                        "    </form>\n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Path directoryPath = Paths.get(directory.getPath());
        Part part = req.getParts().stream().findFirst().orElseThrow();
        String fileName = String.format("%s_%s.pdf", part.getSubmittedFileName().replaceAll(".pdf", ""), Instant.now().getEpochSecond());
        part.write(directoryPath.resolve(fileName).toString());
        AccessRule accessRule = new AccessRule(fileName, Long.parseLong(req.getParameter("ttl")));
        try (PrintWriter p = new PrintWriter(new FileOutputStream(directoryPath.resolve(fileName.hashCode() + ".yaml").toString()))) {
            yaml.dump(accessRule, p);
        }
        PrintWriter writer = resp.getWriter();
        String location = "/docs/" + fileName.hashCode() ;
        writer.print("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body style=\"background: darkgray; display: flex; margin: 1em; justify-content: center;\">\n" +
                "<div id=\"id-form\" class=\"container\"\n" +
                "     style=\"display: block; max-width: 600px;border: 1px solid aliceblue; padding: 1em; border-radius: 10px; background: white; box-shadow: 0 1px  20px 10px #808080;\">\n" +
                "    ✅ the file is accessible\n" +
                "    <a id=\"access\" href=\"" + location + "\">with this link</a>\n" +
                "</div>\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>");
    }
}