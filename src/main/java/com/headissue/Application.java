package com.headissue;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

import com.headissue.domain.AccessRule;
import com.headissue.service.TestDataService;
import com.headissue.servlet.ServletHandlerBuilder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);
  private static Server server;
  private static int port;

  public static final Yaml yaml;

  static {
    DumperOptions options = new DumperOptions();
    options.setIndent(2);
    options.setPrettyFlow(true);
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    yaml = new Yaml(new AccessRule.Representer(options), options);
  }

  public static void main(String[] args) throws IOException, URISyntaxException {
    port = EnvironmentVariables.getAsInt("PORT", 8080);
    File directory = getFileStoreFromEnv();
    new TestDataService(directory, yaml).writeStaticTestFiles();
    server = new Server(port);
    server.setHandler(buildHandler(directory));
    startServer();
    awaitTermination();
  }

  private static void startServer() {
    try {
      server.start();
      logger.info("listening on http://localhost:" + port);
    } catch (Exception e) {
      logger.error("startup", e);
      System.exit(1);
    }
  }

  private static void awaitTermination() {
    try {
      server.join();
    } catch (InterruptedException ex) {
      logger.info("interrupt", ex);
    } catch (Exception ex) {
      logger.error("unhandled exception", ex);
      System.exit(1);
    } finally {
      server.destroy();
    }
  }

  private static ServletContextHandler buildHandler(File directory) {
    ServletContextHandler servletContextHandler = buildServletContextHandler();
    ServletHandlerBuilder builder = new ServletHandlerBuilder(servletContextHandler);
    builder.addForwardIdToDocumentFilter();
    builder.addPdfUpload(directory);
    builder.addTemplateRenderingAndStaticResources(directory);
    return builder.getServletHandler();
  }

  private static ServletContextHandler buildServletContextHandler() {
    ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
    servletContextHandler.setContextPath("/");
    Function<URL, URI> urlToUri =
        it -> {
          try {
            return it.toURI();
          } catch (URISyntaxException e) {
            throw new RuntimeException(e);
          }
        };
    Function<String, URL> getResource = it -> Application.class.getResource("/" + it);
    Function<URI, Resource> uriToResource =
        it -> {
          try {
            return Resource.newResource(it);
          } catch (MalformedURLException e) {
            throw new RuntimeException(e);
          }
        };
    servletContextHandler.setBaseResource(
        new ResourceCollection(
            Stream.of("static")
                .map(getResource)
                .filter(Objects::nonNull)
                .map(urlToUri)
                .map(uriToResource)
                .collect(Collectors.toList())));
    return servletContextHandler;
  }

  private static File getFileStoreFromEnv() {
    return getDirectoryFromEnvAndTestReadWriteAccess();
  }

  private static File getDirectoryFromEnvAndTestReadWriteAccess() {
    String fileStoreDirectory = System.getenv().get("FILE_STORE_DIR");
    File directory = Paths.get(fileStoreDirectory).toFile();
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException();
    }
    try {
      Path pathToTestFile = Paths.get(directory.getPath(), "testReadWriteDelete.txt");
      Files.deleteIfExists(pathToTestFile);
      Files.createFile(pathToTestFile);
      Files.delete(pathToTestFile);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return directory;
  }
}
