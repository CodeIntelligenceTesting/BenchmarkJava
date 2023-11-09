package org.owasp.benchmark.testcode;

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;
import hthurow.tomcatjndi.TomcatJNDI;
import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.benchmark.helpers.Startup;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

class BenchmarkTest00112FuzzTest {

  static TestHttpServer server;

  @FuzzTest
  void doPost(FuzzedDataProvider data) throws ServletException, IOException {
    BenchmarkTest02246 servlet = new BenchmarkTest02246();
    MockServletContext context = new MockServletContext();
    MockHttpServletRequest request = post("https://localhost")
        .cookie(consumeCookie(data))
        .buildRequest(context);
    MockHttpServletResponse response = new MockHttpServletResponse();
    servlet.doPost(request, response);
    assertThat(response.getContentAsString()).doesNotContain("Welcome back");
  }

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_VALUE_LENGTH = 100;

  private static final char[] VALID_COOKIE_NAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!#%&'*+-.^_`|~".toCharArray();

  private static Cookie consumeCookie(FuzzedDataProvider data) {
    int nameLength = data.consumeInt(1, MAX_NAME_LENGTH);
    char[] nameChars = new char[nameLength];
    for (int i = 0; i < nameLength; i++) {
      nameChars[i] = data.pickValue(VALID_COOKIE_NAME_CHARS);
    }
    return new Cookie(new String(nameChars), data.consumeString(MAX_VALUE_LENGTH));
  }
}