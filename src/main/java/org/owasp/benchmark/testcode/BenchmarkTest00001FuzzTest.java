package org.owasp.benchmark.testcode;

import static org.junit.jupiter.api.Assertions.*;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

class BenchmarkTest00001FuzzTest {

    static private Context context;
    static private Tomcat tomcat;
    static private String contextPath;

    @BeforeAll
    static void setup() {
        tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(8080);

        contextPath = "/";
        String docBase = new File(".").getAbsolutePath();

        context = tomcat.addContext(contextPath, docBase);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void startupTomcat() {

        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                PrintWriter writer = resp.getWriter();

                writer.println("<html><title>Welcome</title><body>");
                writer.println("<h1>Have a Great Day!</h1>");
                writer.println("</body></html>");
            }
        };

        String servletName = "Servlet1";
        String urlPattern = "/go";

        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded(urlPattern, servletName);

        tomcat.getServer().await();
    }

    @FuzzTest
    void fuzzTest(HttpServletRequest request, HttpServletResponse response) {
        try {
            new BenchmarkTest00001().doPost(request, response);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
