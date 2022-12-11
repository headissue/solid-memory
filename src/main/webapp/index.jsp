<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<h1>It works!</h1>
<%
    for (int i = 0; i < 5; ++i) {
        out.println("<p>Hello, world!</p>");
    }
%>