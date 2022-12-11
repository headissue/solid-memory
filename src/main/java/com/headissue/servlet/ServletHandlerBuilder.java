package com.headissue.servlet;

import com.headissue.Application;
import com.headissue.filter.DocumentIdForwardingFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import java.io.File;
import java.util.EnumSet;
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

  public void addForwardIdToDocument() {
    FilterHolder docIdFilter = new FilterHolder(new DocumentIdForwardingFilter());
    servletHandler.addFilter(docIdFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
  }

  public void addDefaultJspResolution() {
    ResourceService resourceService = new ResourceService();
    DefaultServlet defaultServlet = new DefaultServlet(resourceService);
    ServletHolder jsp = new ServletHolder("jsp", defaultServlet);
    servletHandler.addServlet(jsp, "/*");
  }
}
