package com.headissue.servlet;

import com.github.jknack.handlebars.Handlebars;
import com.headissue.Application;
import com.headissue.filter.DocumentIdForwardingFilter;
import com.headissue.service.FormKeyService;
import jakarta.servlet.*;
import java.io.File;
import java.util.*;
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

  public void addForwardIdToDocumentFilter() {
    FilterHolder docIdFilter = new FilterHolder(new DocumentIdForwardingFilter());
    servletHandler.addFilter(docIdFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  public void addTemplateRenderingAndStaticResources(Handlebars handlebars) {
    TemplateRendering templateRendering = new TemplateRendering(handlebars);
    ServletRegistration.Dynamic renderTemplate =
        servletHandler.getServletContext().addServlet("handlebars", templateRendering);
    renderTemplate.addMapping("/*");

    ResourceService resourceService = new ResourceService();
    DefaultServlet defaultServlet = new DefaultServlet(resourceService);
    servletHandler.addServlet(new ServletHolder("static/img", defaultServlet), "/img/*");
    servletHandler.addServlet(new ServletHolder("static/css", defaultServlet), "/css/*");
    servletHandler.addServlet(new ServletHolder("static/js", defaultServlet), "/js/*");
    servletHandler.addServlet(new ServletHolder("static/js", defaultServlet), "/favicon.ico");
  }

  public void addSeePdfs(File directory, Handlebars handlebars) {
    ServletRegistration.Dynamic seePdfs =
        servletHandler
            .getServletContext()
            .addServlet(
                "seePdfs",
                new SeePdfs(
                    directory,
                    handlebars,
                    LoggerFactory.getLogger("pdf"),
                    Application.yaml,
                    new FormKeyService()));
    seePdfs.addMapping("/docs/*");
    seePdfs.setMultipartConfig(
        new MultipartConfigElement(directory.getPath(), 1024 * 1024 * 10, 1024 * 1024 * 10, 0));
  }
}
