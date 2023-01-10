package com.headissue.servlet;

import com.github.jknack.handlebars.Handlebars;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.jetty.http.MimeTypes;

public class TemplateRendering extends HttpServlet {

  private final Handlebars handlebars;

  public TemplateRendering(Handlebars handlebars) {
    this.handlebars = handlebars;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
    String pathInfo = req.getPathInfo();
    if (pathInfo.equals("/")) {
      handlebars.compile("index.hbs").apply(null, resp.getWriter());
    } else {
      try {
        handlebars.compile(pathInfo + ".hbs").apply(null, resp.getWriter());
      } catch (FileNotFoundException e) {
        req.getRequestDispatcher("/404").forward(req, resp);
      }
    }
  }
}
