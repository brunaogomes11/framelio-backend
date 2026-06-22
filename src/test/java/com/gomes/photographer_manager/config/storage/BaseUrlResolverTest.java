package com.gomes.photographer_manager.config.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseUrlResolverTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void resolveUsesCurrentRequestHostWhenBaseUrlNotConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("vps.example.com");
        request.setServerPort(443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("https://vps.example.com", new BaseUrlResolver("").resolve());
    }

    @Test
    void resolvePrefersConfiguredBaseUrlAndStripsTrailingSlash() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("internal-host");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        BaseUrlResolver resolver = new BaseUrlResolver("https://api-dev-framelio.gomesdev.tech/");

        assertEquals("https://api-dev-framelio.gomesdev.tech", resolver.resolve());
    }

    @Test
    void resolveFallsBackToLocalhostWhenNoRequestAndNoConfig() {
        assertEquals("http://localhost:8080", new BaseUrlResolver("").resolve());
    }
}
