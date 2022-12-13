package com.headissue.servlet;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.headissue.Application;
import com.headissue.filter.DocumentIdForwardingFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.ResourceService;
import org.eclipse.jetty.servlet.*;
import org.slf4j.LoggerFactory;

public class ServletHandlerBuilder {
  private final ServletContextHandler servletHandler;

  public ServletHandlerBuilder(ServletContextHandler servletHandler) {
    this.servletHandler = servletHandler;
  }

  public ServletContextHandler getServletHandler() {
    return servletHandler;
  }

  public void addDocumentViewer(File directory) {
    ServletRegistration.Dynamic servePdf =
        servletHandler
            .getServletContext()
            .addServlet(
                "servePdf",
                new ServesPdfs(
                    directory, LoggerFactory.getLogger(ServesPdfs.class), Application.yaml));
    servePdf.setLoadOnStartup(1);
    servePdf.addMapping("/docs/*");
    servePdf.setMultipartConfig(new MultipartConfigElement(directory.getPath(), 0, 1024, 0));
  }

  public void addPdfUpload(File directory) {
    ServletRegistration.Dynamic savePdf =
        servletHandler
            .getServletContext()
            .addServlet("savePdf", new SavesPdfs(directory, Application.yaml));
    savePdf.setLoadOnStartup(1);
    savePdf.addMapping("/public/share");
    savePdf.setMultipartConfig(
        new MultipartConfigElement(directory.getPath(), 1024 * 1024 * 10, 1024 * 1024 * 10, 0));
  }

  public void addIdForm() {
    servletHandler.addServlet(new ServletHolder(new ServesIdForm()), "/public/idForm");
  }

  public void addForwardIdToDocumentFilter() {
    FilterHolder docIdFilter = new FilterHolder(new DocumentIdForwardingFilter());
    servletHandler.addFilter(docIdFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  public void addTemplateRenderingAndStaticResources() {
    servletHandler.addServlet(
        new ServletHolder(
            "handlebars",
            new HttpServlet() {
              @Override
              protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                  throws IOException {
                resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
                Handlebars handlebars =
                    new Handlebars(new ClassPathTemplateLoader("/static", ".hbs"));
                String pathInfo = req.getPathInfo();
                if (pathInfo.equals("/")) {
                  handlebars.compile("index").apply(null, resp.getWriter());
                } else {
                  handlebars.compile(pathInfo).apply(null, resp.getWriter());
                }
              }
            }),
        "/*");
    ResourceService resourceService = new ResourceService();
    DefaultServlet defaultServlet = new DefaultServlet(resourceService);
    servletHandler.addServlet(new ServletHolder("static", defaultServlet), "/img/*");
    servletHandler.addServlet(new ServletHolder("static", defaultServlet), "/js/*");
  }
}
