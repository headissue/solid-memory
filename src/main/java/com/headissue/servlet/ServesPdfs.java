package com.headissue.servlet;

import com.headissue.domain.AccessRule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.eclipse.jetty.util.StringUtil.isBlank;

public class ServesPdfs extends HttpServlet {

    private final File directory;
    private final Logger accessReporter;
    private final Yaml yaml;

    {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(options);
    }

    public ServesPdfs(File directory, Logger accessReporter) {
        this.directory = directory;
        this.accessReporter = accessReporter;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accessId = req.getPathInfo().substring("/".length());
        Path accessYaml = Paths.get(directory.getPath(), accessId + ".yaml");
        if (Files.notExists(accessYaml)) {
            req.getRequestDispatcher("/404.html").forward(req, resp);
            return;
        }
        if (isExpired(accessYaml)) {
            req.getRequestDispatcher("/404.html").forward(req, resp);
            return;
        }

        String key = "id";
        boolean noParameter = isMissingRequiredParameter(req, key);
        if (noParameter) {
            req.getRequestDispatcher("/public/idForm").forward(req, resp);
            return;
        }

        AccessRule accessRule = yaml.load(new FileInputStream(accessYaml.toFile()));
        accessReporter.info("access: " + accessRule.getFileName() + "; " + "by: " + req.getParameter(key));


        PrintWriter writer = resp.getWriter();
        resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());

        byte[] bytes = Files.readAllBytes(Paths.get(directory.getPath(), accessRule.getFileName()));
        byte[] encoded = java.util.Base64.getEncoder().encode(bytes);
        String base64Pdf = new String(encoded);
        writer.print(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>your doc</title>\n" +
                        "    <style>\n" +
                        "        body, html {width: 100%; height: 100%; margin: 0; padding: 0}\n" +
                        "        .container {display: flex; width: 100%; height: 100%; flex-direction: column; background-color: #c7c7c7; overflow: hidden;}\n" +
                        "        .pdf { flex-grow: 1; border: none; margin: 0; padding: 0; }\n" +
                        "    </style>\n" +
                        "    <script src=\"https://unpkg.com/pdfjs-dist@3.1.81/build/pdf.min.js\"></script>\n" +
                        "\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<div style=\"position: fixed; color: white; background: black; width: 100%;padding: 5px;\">\n" +
                        "    <button id=\"prev\">Previous</button>\n" +
                        "    <button id=\"next\">Next</button>\n" +
                        "    <span>Page: <span id=\"page_num\"></span> / <span id=\"page_count\"></span></span>\n" +
                        "</div>\n" +
                        "\n" +
                        "<div style=\"width: 98%; margin: 0; padding: 1%; background: #c7c7c7; padding-top:3em\">\n" +
                        "    <canvas style=\"width: 100%\" id=\"the-canvas\"></canvas>\n" +
                        "</div>\n" +
                        "<script type=\"text/javascript\">\n" +
                        "\n" +
                        "\n" +
                        "    // atob() is used to convert base64 encoded PDF to binary-like data.\n" +
                        "    // (See also https://developer.mozilla.org/en-US/docs/Web/API/WindowBase64/\n" +
                        "    // Base64_encoding_and_decoding.)\n" +
                        "    var pdfData = atob(\n" +
                        "        '" +
                        base64Pdf +
                        "');\n" +
                        "\n" +
                        "\n" +
                        "    var pdfDoc = null,\n" +
                        "        pageNum = 1,\n" +
                        "        pageRendering = false,\n" +
                        "        pageNumPending = null,\n" +
                        "        scale = 4,\n" +
                        "        canvas = document.getElementById('the-canvas'),\n" +
                        "        ctx = canvas.getContext('2d');\n" +
                        "\n" +
                        "    /**\n" +
                        "     * Get page info from document, resize canvas accordingly, and render page.\n" +
                        "     * @param num Page number.\n" +
                        "     */\n" +
                        "    function renderPage(num) {\n" +
                        "        pageRendering = true;\n" +
                        "        // Using promise to fetch the page\n" +
                        "        pdfDoc.getPage(num).then(function(page) {\n" +
                        "            var viewport = page.getViewport({scale: scale});\n" +
                        "            canvas.height = viewport.height;\n" +
                        "            canvas.width = viewport.width;\n" +
                        "\n" +
                        "            // Render PDF page into canvas context\n" +
                        "            var renderContext = {\n" +
                        "                canvasContext: ctx,\n" +
                        "                viewport: viewport\n" +
                        "            };\n" +
                        "            var renderTask = page.render(renderContext);\n" +
                        "\n" +
                        "            // Wait for rendering to finish\n" +
                        "            renderTask.promise.then(function() {\n" +
                        "                pageRendering = false;\n" +
                        "                if (pageNumPending !== null) {\n" +
                        "                    // New page rendering is pending\n" +
                        "                    renderPage(pageNumPending);\n" +
                        "                    pageNumPending = null;\n" +
                        "                }\n" +
                        "            });\n" +
                        "        });\n" +
                        "\n" +
                        "        // Update page counters\n" +
                        "        document.getElementById('page_num').textContent = num;\n" +
                        "    }\n" +
                        "\n" +
                        "    /**\n" +
                        "     * If another page rendering in progress, waits until the rendering is\n" +
                        "     * finised. Otherwise, executes rendering immediately.\n" +
                        "     */\n" +
                        "    function queueRenderPage(num) {\n" +
                        "        if (pageRendering) {\n" +
                        "            pageNumPending = num;\n" +
                        "        } else {\n" +
                        "            renderPage(num);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    /**\n" +
                        "     * Displays previous page.\n" +
                        "     */\n" +
                        "    function onPrevPage() {\n" +
                        "        if (pageNum <= 1) {\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        pageNum--;\n" +
                        "        queueRenderPage(pageNum);\n" +
                        "    }\n" +
                        "    document.getElementById('prev').addEventListener('click', onPrevPage);\n" +
                        "\n" +
                        "    /**\n" +
                        "     * Displays next page.\n" +
                        "     */\n" +
                        "    function onNextPage() {\n" +
                        "        if (pageNum >= pdfDoc.numPages) {\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        pageNum++;\n" +
                        "        queueRenderPage(pageNum);\n" +
                        "    }\n" +
                        "    document.getElementById('next').addEventListener('click', onNextPage);\n" +
                        "\n" +
                        "    /**\n" +
                        "     * Asynchronously downloads PDF.\n" +
                        "     */\n" +
                        "    pdfjsLib.getDocument({data: pdfData}).promise.then(function(pdfDoc_) {\n" +
                        "        pdfDoc = pdfDoc_;\n" +
                        "        document.getElementById('page_count').textContent = pdfDoc.numPages;\n" +
                        "\n" +
                        "        // Initial/first page rendering\n" +
                        "        renderPage(pageNum);\n" +
                        "    });\n" +
                        "\n" +
                        "</script>\n" +
                        "</body>\n" +
                        "</html>");
    }

    private static boolean isMissingRequiredParameter(HttpServletRequest req, String key) {
        Map<String, String[]> parameters = req.getParameterMap();
        Set<String> keys = parameters.keySet();
        if (keys.isEmpty()) {
            return true;
        }
        if (!keys.contains(key)) {
            return true;
        }
        return isBlank(req.getParameter(key));
    }

    private boolean isExpired(Path accessYaml) throws IOException {
        AccessRule accessRule = yaml.load(new FileInputStream(accessYaml.toFile()));
        Path pdf = Paths.get(directory.getPath(), accessRule.getFileName());
        Instant now = Instant.now();
        BasicFileAttributes attributes = Files.readAttributes(pdf, BasicFileAttributes.class);
        FileTime fileTime = attributes.creationTime();

        Instant ttl = fileTime.toInstant().plusSeconds(accessRule.getTtlSeconds());
        return ttl.isBefore(now);
    }
}
