package com.headissue.filter;

import static java.lang.String.format;

import com.headissue.config.NanoIdConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DocumentIdForwardingFilter extends HttpFilter {
  @Override
  protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String requestURI = req.getRequestURI();
    if (requestURI == null) {
      super.doFilter(req, res, chain);
      return;
    }
    if (requestURI.length() != "/".length() + NanoIdConfig.length) {
      super.doFilter(req, res, chain);
      return;
    }
    String withoutLeadingSlash = requestURI.substring("/".length());
    if (withoutLeadingSlash.matches(format("^.{%d}$", NanoIdConfig.length))) {
      String alphabet = new String(NanoIdConfig.alphabet);
      if (withoutLeadingSlash.chars().allMatch(c -> alphabet.indexOf(c) >= 0)) {
        req.getRequestDispatcher("/docs" + requestURI).forward(req, res);
        return;
      }
    }
    super.doFilter(req, res, chain);
  }
}
