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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

class SavesPdfsTest {

    @TempDir
    static Path sharedTempDir;


    SavesPdfs sut;

    HttpServletRequest request;
    HttpServletResponse response;

    @BeforeAll
    static void setUpClass() {

    }

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
        response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
        sut = new SavesPdfs(sharedTempDir.toFile());
    }

    @Test
    void whereOpeningLiveLinkWithIdParameterReportAccess() throws ServletException, IOException {
        when(request.getParameterMap()).thenReturn(Map.of("id", new String[]{"email@example.com"}));
        when(request.getParameter("id")).thenReturn("email@example.com");
        sut.doGet(request, response);
    }
}