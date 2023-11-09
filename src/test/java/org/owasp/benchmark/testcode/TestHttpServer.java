package org.owasp.benchmark.testcode;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Allows to create a simple servlet-based server on the fly -- useful for tests that
 * need to talk with a real server
 *
 * ex : <code>
 *     TestHttpServer server = new TestHttpServer()
 *     server.addStatic("/hello", "Hello, world !");
 *     server.start();
 *
 *     URL url = server.makeUrl("/hello");
 *
 *     String body = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
 *     Assert.assertEquals("Hello, world !", body);
 *
 *     [...]
 *
 *     server.stop();
 *
 * <pre class="dependencies"
 * testCompile 'org.apache.tomcat.embed:tomcat-embed-core:8.5.2'
 * testCompile 'org.apache.tomcat.embed:tomcat-embed-logging-juli:8.5.2'
 * testCompile 'org.springframework:spring-test:4.3.8.RELEASE'
 * </pre>
 *
 * </code>
 */
public class TestHttpServer {

  private Tomcat tomcat;

  private File tempDir;

  private Map<String, Servlet> servlets = new HashMap<>();

  public void start() {

    try {
      tempDir = File.createTempFile("tomcat", ".tmp");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (!tempDir.mkdirs()) {
      throw new IllegalStateException("could not create temp dir");
    }

    tomcat = new Tomcat();
    tomcat.setBaseDir(tempDir.getAbsolutePath());
    tomcat.getHost().setAutoDeploy(true);
    tomcat.getHost().setDeployOnStartup(true);

    final Connector connector = new Connector("HTTP/1.1");
    connector.setAttribute("address", "localhost");
    connector.setPort(0);
    tomcat.setConnector(connector);
    tomcat.getService().addConnector(connector);



    try {
      tomcat.start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    File rootDir = new File(tempDir, "ROOT");
    if (!rootDir.mkdirs()) {
      throw new IllegalStateException("could not create root dir");
    }
    Context rootContext = tomcat.addContext("/", rootDir.getAbsolutePath());
    for (Map.Entry<String, Servlet> entry : servlets.entrySet()) {
      Tomcat.addServlet(rootContext, entry.getValue().toString(), entry.getValue()).addMapping(entry.getKey());
    }
  }

  private static void deleteDirectory(File f) {
    if(f.isDirectory()) {
      f.delete();
      File[] files = f.listFiles();
      if(files != null) {
        for(File child : files) {
          deleteDirectory(f);
        }
      }
      f.delete();
    } else {
      f.delete();
    }
  }

  public int getPort() {
    return tomcat.getConnector().getLocalPort();
  }

  public URL makeUrl(String path) {
    final InetAddress addr = (InetAddress) tomcat.getConnector().getAttribute("address");
    try {
      return new URL("http://" + addr.getHostAddress() + ":" + getPort() + (path.startsWith("/")?path : "/"+path));
    } catch(MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
      tomcat.stop();
      tomcat.destroy();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    deleteDirectory(tempDir);
    tempDir = null;
  }

  public TestHttpServer addServlet(String mapping, Servlet servlet) {
    MockServletContext mockServletContext = new MockServletContext("/");
    MockServletConfig config = new MockServletConfig(mockServletContext, servlet.toString());
    try {
      servlet.init(config);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    servlets.put(mapping, servlet);
    return this;
  }

  public TestHttpServer addStatic(String mapping, final String data) {
    return addStatic(mapping, new Callable<InputStream>() {
      @Override
      public InputStream call() throws Exception {
        return new ByteArrayInputStream(data.getBytes());
      }
    });
  }


  public TestHttpServer addStatic(String mapping, final InputStream data) {
    return addStatic(mapping, new Callable<InputStream>() {
      @Override
      public InputStream call() throws Exception {
        return data;
      }
    });
  }

  public TestHttpServer addStatic(String mapping, final Callable<InputStream> data) {
    return addServlet(mapping, new HttpServlet() {
      @Override
      protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("appplication/octet-stream");
        try {
          slurp(data.call(), resp.getOutputStream());
        } catch (Exception e) {
          throw new ServletException(e);
        }
      }
    });
  }

  private static void slurp(InputStream is, OutputStream os) throws IOException {
    byte[] buff = new byte[4096];
    int c = 0;
    while ((c = is.read(buff)) > -1) {
      os.write(buff, 0, c);
    }
    os.flush();
  }
}