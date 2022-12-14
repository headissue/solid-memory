package com.headissue.servlet;

import com.headissue.Application;
import com.headissue.filter.DocumentIdForwardingFilter;
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

  public void addTemplateRenderingAndStaticResources(File directory) {
    TemplateRendering templateRendering =
        new TemplateRendering(directory, LoggerFactory.getLogger("pdf access"), Application.yaml);
    ServletRegistration.Dynamic servePdf =
        servletHandler.getServletContext().addServlet("handlebars", templateRendering);
    servePdf.addMapping("/*");
    servePdf.setMultipartConfig(
        new MultipartConfigElement(directory.getPath(), 1024 * 1024 * 10, 1024 * 1024 * 10, 0));

    ResourceService resourceService = new ResourceService();
    DefaultServlet defaultServlet = new DefaultServlet(resourceService);
    servletHandler.addServlet(new ServletHolder("static/img", defaultServlet), "/img/*");
    servletHandler.addServlet(new ServletHolder("static/css", defaultServlet), "/css/*");
    servletHandler.addServlet(new ServletHolder("static/js", defaultServlet), "/js/*");
  }
}
