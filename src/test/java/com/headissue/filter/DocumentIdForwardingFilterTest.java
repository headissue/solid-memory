package com.headissue.filter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocumentIdForwardingFilterTest {

  DocumentIdForwardingFilter sut;

  HttpServletRequest request;
  HttpServletResponse response;
  FilterChain chain;
  RequestDispatcher requestDispatcher;

  @BeforeAll
  static void setUpClass() {}

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
    requestDispatcher = mock(RequestDispatcher.class, RETURNS_DEEP_STUBS);
    response = mock(HttpServletResponse.class, RETURNS_DEEP_STUBS);
    sut = new DocumentIdForwardingFilter();
    chain = mock(FilterChain.class, RETURNS_DEEP_STUBS);

    when(request.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);
  }

  @Test
  void whereThereIsNoRequestUri() throws ServletException, IOException {
    sut.doFilter(request, response, chain);
    verifyNotForwarded();
  }

  @Test
  void whereRequestUriIsRoot() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/");
    sut.doFilter(request, response, chain);
    verifyNotForwarded();
  }

  @Test
  void whereRequestUriDoesNotMatchNanoId() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/1234567|");
    sut.doFilter(request, response, chain);
    verifyNotForwarded();
  }

  @Test
  void whereRequestUriDoesMatchNanoId() throws ServletException, IOException {
    when(request.getRequestURI()).thenReturn("/a1B2c_D-");
    sut.doFilter(request, response, chain);
    verify(request, times(1)).getRequestDispatcher("/docs/a1B2c_D-");
    verify(requestDispatcher, times(1)).forward(request, response);
  }

  private void verifyNotForwarded() throws IOException, ServletException {
    verify(chain, times(1)).doFilter(request, response);
    verify(request, times(0)).getRequestDispatcher(anyString());
  }
}
