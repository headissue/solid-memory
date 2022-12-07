package com.headissue.servlet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.MimeTypes;

import java.io.IOException;
import java.io.PrintWriter;

public class ServesIdForm extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        DispatcherType dispatcherType = req.getDispatcherType();
        if (!dispatcherType.equals(DispatcherType.FORWARD)) {
            req.getRequestDispatcher("/404.html").forward(req, resp);
            return;
        }

        PrintWriter writer = resp.getWriter();
        resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());

      writer.print("<!DOCTYPE html>\n" +
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
              "    <form onsubmit=\"function setAction(form) {\n" +
              "        form.action = window.location.href\n" +
              "        return form.submit;\n" +
              "    }\n" +
              "    return setAction(this)\">\n" +
              "        <label>\n" +
              "            email:\n" +
              "            <input type=\"email\" name=\"id\">\n" +
              "        </label>\n" +
              "        <button type=\"submit\">SEND</button>\n" +
              "    </form>\n" +
              "</div>\n" +
              "</body>\n" +
              "</html>");

    }
}
