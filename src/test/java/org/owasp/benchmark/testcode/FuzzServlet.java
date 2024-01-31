package org.owasp.benchmark.testcode;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

public class FuzzServlet {

    private static final int MAX_NAME_LENGTH = 30;
    private static final int MAX_VALUE_LENGTH = 100;
    private static final int MAX_ENTRY_LENGTH = 10;

    // TODO: Should this be [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!#%&'*+-.^_`|~]+
    private static final Pattern VALID_COOKIE_NAME_CHARS = Pattern.compile("[abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789]+");
    private static final List<String> methods = Arrays.asList("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH");
    private static final URI MOCK_URL = URI.create("http://localhost");

    public static MockHttpServletResponse fuzzServlet(FuzzedDataProvider data, HttpServlet servlet) throws Exception {
        String method = data.pickValue(methods);
        MockHttpServletRequestBuilder builder = request(method, MOCK_URL);
        MockHttpServletRequest request = consumeRequest(builder, data);
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(request, response);
        return response;
    }

    public static MockHttpServletRequest consumeRequest(MockHttpServletRequestBuilder requestBuilder, FuzzedDataProvider data) {
        consumeCookies(requestBuilder, data);
        consumeProperties(data, requestBuilder::header);
        consumeProperties(data, requestBuilder::param);
        consumeProperties(data, requestBuilder::queryParam);
        consumeProperties(data, requestBuilder::sessionAttr);
        // Last call to FuzzedDataProvider with consumeRemaining!
        requestBuilder.content(data.consumeRemainingAsBytes());
        return requestBuilder.buildRequest(new MockServletContext());
    }

    private static void consumeCookies(MockHttpServletRequestBuilder requestBuilder, FuzzedDataProvider data) {
        for (int i = 0; i < data.consumeInt(0, MAX_ENTRY_LENGTH); i++) {
            requestBuilder.cookie(consumeCookie(data));
        }
    }

    private static Cookie consumeCookie(FuzzedDataProvider data) {
        String name = data.consumeAsciiString(MAX_NAME_LENGTH);
        assumeTrue(VALID_COOKIE_NAME_CHARS.matcher(name).matches());
        Cookie cookie = new Cookie(name, data.consumeString(MAX_VALUE_LENGTH));
        cookie.setHttpOnly(data.consumeBoolean());
        cookie.setSecure(data.consumeBoolean());
        cookie.setPath(data.consumeString(MAX_VALUE_LENGTH));
        cookie.setDomain(data.consumeString(MAX_VALUE_LENGTH));
        cookie.setMaxAge(data.consumeInt(0, 1000000));
        return cookie;
    }

    public static void consumeProperties(FuzzedDataProvider data, BiConsumer<String, String> func) {
        for (int i = 0; i < data.consumeInt(0, MAX_ENTRY_LENGTH); i++) {
            String name = data.consumeString(MAX_NAME_LENGTH);
            String value = data.consumeString(MAX_VALUE_LENGTH);
            if (name != null && !name.isEmpty()) {
                func.accept(name, value);
            }
        }
    }
}
