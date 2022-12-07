package com.headissue.servlet;

import com.headissue.Application;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.ContentType;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServesPdfs extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private File directory;

    public ServesPdfs(File directory) {
        this.directory = directory;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());

        byte[] inFileBytes = Files.readAllBytes(Paths.get(directory.getPath(), "handreichung_elternbeiraete_web.pdf"));
        byte[] encoded = java.util.Base64.getEncoder().encode(inFileBytes);

        //String base64Pdf = "JVBERi0xLjcKCjEgMCBvYmogICUgZW50cnkgcG9pbnQKPDwKICAvVHlwZSAvQ2F0YWxvZwogIC9QYWdlcyAyIDAgUgo+PgplbmRvYmoKCjIgMCBvYmoKPDwKICAvVHlwZSAvUGFnZXMKICAvTWVkaWFCb3ggWyAwIDAgMjAwIDIwMCBdCiAgL0NvdW50IDEKICAvS2lkcyBbIDMgMCBSIF0KPj4KZW5kb2JqCgozIDAgb2JqCjw8CiAgL1R5cGUgL1BhZ2UKICAvUGFyZW50IDIgMCBSCiAgL1Jlc291cmNlcyA8PAogICAgL0ZvbnQgPDwKICAgICAgL0YxIDQgMCBSIAogICAgPj4KICA+PgogIC9Db250ZW50cyA1IDAgUgo+PgplbmRvYmoKCjQgMCBvYmoKPDwKICAvVHlwZSAvRm9udAogIC9TdWJ0eXBlIC9UeXBlMQogIC9CYXNlRm9udCAvVGltZXMtUm9tYW4KPj4KZW5kb2JqCgo1IDAgb2JqICAlIHBhZ2UgY29udGVudAo8PAogIC9MZW5ndGggNDQKPj4Kc3RyZWFtCkJUCjcwIDUwIFRECi9GMSAxMiBUZgooSGVsbG8sIHdvcmxkISkgVGoKRVQKZW5kc3RyZWFtCmVuZG9iagoKeHJlZgowIDYKMDAwMDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDEwIDAwMDAwIG4gCjAwMDAwMDAwNzkgMDAwMDAgbiAKMDAwMDAwMDE3MyAwMDAwMCBuIAowMDAwMDAwMzAxIDAwMDAwIG4gCjAwMDAwMDAzODAgMDAwMDAgbiAKdHJhaWxlcgo8PAogIC9TaXplIDYKICAvUm9vdCAxIDAgUgo+PgpzdGFydHhyZWYKNDkyCiUlRU9G";
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
                        "<div>\n" +
                        "    <button id=\"prev\">Previous</button>\n" +
                        "    <button id=\"next\">Next</button>\n" +
                        "    <span>Page: <span id=\"page_num\"></span> / <span id=\"page_count\"></span></span>\n" +
                        "</div>\n" +
                        "\n" +
                        "<div style=\"width: 98%; margin: 0; padding: 1%; background: #c7c7c7\">\n" +
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
                        "        scale = 1,\n" +
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
}
