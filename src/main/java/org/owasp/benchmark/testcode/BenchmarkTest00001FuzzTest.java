package org.owasp.benchmark.testcode;

import com.code_intelligence.jazzer.junit.FuzzTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
    //    A context path in Apache Tomcat refers to the name of the website as presented by the browser.
    //    For example, imagine I tell you to enter "localhost:8080/DemoWebsite/DateJSP. jsp" in your browser. The context path is "DemoWebsite"
    final static private String contextPath = "";


    @BeforeAll
    static void setup() {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String webappDirLocation = "src/main/webapp/";
//        String webappDirLocation = "/DOC_BASE";
        String docBase = new File(webappDirLocation).getAbsolutePath();
        context = tomcat.addContext(contextPath, docBase);
        System.out.println(context.getPath());

        WebResourceRoot resources = new StandardRoot(context);
        System.out.println(docBase);

        resources.addPreResources(new DirResourceSet(resources, "/WEB_APP_MOUNT",
                docBase, "/"));
        
        context.setResources(resources);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void startupTomcat() {

        HttpServlet servlet = new BenchmarkTest00001();

        String servletName = "BenchmarkTest00001";
        String urlPattern = "/pathtraver-00/BenchmarkTest00001";

        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded(urlPattern, servletName);

        final OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080" + urlPattern)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        tomcat.getServer().await();
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
