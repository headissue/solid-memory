package com.headissue.servlet;

import com.headissue.domain.AccessRule;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import static org.mockito.Mockito.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

class ServesPdfsTest {

    @TempDir
    static Path sharedTempDir;


    Logger accessReporter;


    ServesPdfs sut;

    HttpServletRequest request;
    HttpServletResponse response;
    RequestDispatcher requestDispatcher;

    @BeforeAll
    static void setUpClass() {
        writeMockAccessFiles();
    }

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
        requestDispatcher = mock(RequestDispatcher.class, RETURNS_DEEP_STUBS);
        response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
        accessReporter = mock(Logger.class, RETURNS_DEEP_STUBS);
        sut = new ServesPdfs(sharedTempDir.toFile(), accessReporter);

        when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
    }

    @Test
    void whereOpeningWrongLinkForwardsToNotFound() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/doesNotExist");
        sut.doGet(request, response);
        verify(request).getRequestDispatcher("/404.html");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void whereOpeningExpiredLinkForwardsToNotFound() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/expired");
        sut.doGet(request, response);
        verify(request).getRequestDispatcher("/404.html");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void whereOpeningLiveLinkWithoutIdParameterForwardsToForm() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/willGrantAccess");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        sut.doGet(request, response);
        verify(request).getRequestDispatcher("/public/idForm");
        verify(requestDispatcher).forward(request, response);
    }

    @Test
    void whereOpeningLiveLinkWithIdParameterReportAccess() throws ServletException, IOException {
        when(request.getPathInfo()).thenReturn("/willGrantAccess");
        when(request.getParameterMap()).thenReturn(Map.of("id", new String[]{"email@example.com"}));
        when(request.getParameter("id")).thenReturn("email@example.com");
        sut.doGet(request, response);
        verify(accessReporter).info("access: test.pdf; by: email@example.com");
    }

    // TODO the whole filestore should be a client so we can mock it and switch underlying tech, mock file generation times etc
    static void writeMockAccessFiles() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (PrintWriter p = new PrintWriter(new FileOutputStream(sharedTempDir.resolve("willGrantAccess.yaml").toFile()))) {
            yaml.dump(new AccessRule("test.pdf", 60), p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (PrintWriter p = new PrintWriter(new FileOutputStream(sharedTempDir.resolve("expired.yaml").toFile()))) {
            yaml.dump(new AccessRule("test.pdf", -1), p);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Files.createFile(sharedTempDir.resolve("test.pdf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}