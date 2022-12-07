package com.headissue;

import com.headissue.domain.AccessRule;
import com.headissue.servlet.Control;
import com.headissue.servlet.ServesIdForm;
import com.headissue.servlet.ServesPdfs;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;


public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        int port = getPortFromEnv();
        File directory = getDirectoryAndTestReadWriteAccess();
        writeStaticTestFiles(directory);

        Server server = new Server(port);

        ServletContextHandler servletHandler = new ServletContextHandler(NO_SESSIONS);

        ServletHolder servePdfs = new ServletHolder(new ServesPdfs(directory));
        ServletHolder serveIdForm = new ServletHolder(new ServesIdForm());

        servletHandler.addServlet(servePdfs, "/docs/*");
        servletHandler.addServlet(serveIdForm, "/public/idForm");
        servletHandler.addServlet(Control.class, "/api");
        server.setHandler(servletHandler);
        try {
            server.start();
            logger.info("started, listening on http://localhost:" + port);
            server.join();
        } catch (Exception ex) {
            logger.error("startup", ex);
            System.exit(1);
        } finally {
            server.destroy();
        }
    }

    private static void writeStaticTestFiles(File directory) {
        Path pdfPath = Paths.get(directory.getPath(), "test.pdf");
        Path yamlPath = Paths.get(directory.getPath(), "test.yaml");
        createIfNotExists(pdfPath);
        try(PrintWriter p = new PrintWriter(new FileOutputStream(yamlPath.toFile()))){
            DumperOptions options = new DumperOptions();
            options.setIndent(2);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            yaml.dump(new AccessRule("test.pdf", 3000000000L ), p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void createIfNotExists(Path pdfPath) {
        if (Files.notExists(pdfPath)) {
            try {
                Files.createFile(pdfPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static File getDirectoryAndTestReadWriteAccess() {
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

    private static int getPortFromEnv() {
        String portEnvVar = System.getenv().get("PORT");
        int port = 8080;
        if (portEnvVar != null && !portEnvVar.equals("")) {
            port = Integer.parseInt(portEnvVar);
        }
        return port;
    }
}
