package com.gomes.photographer_manager.domain.auth.refresh;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Monta o cookie httpOnly que carrega o refresh token.
 * Escopo em /auth para que o cookie só seja enviado aos endpoints de autenticação.
 */
@Component
public class RefreshTokenCookieFactory {

    public static final String COOKIE_NAME = "refreshToken";
    private static final String COOKIE_PATH = "/auth";

    private final String domain;
    private final boolean secure;
    private final String sameSite;
    private final Duration maxAge;

    public RefreshTokenCookieFactory(
            @Value("${app.cookie.domain:}") String domain,
            @Value("${app.cookie.secure:true}") boolean secure,
            @Value("${app.cookie.same-site:None}") String sameSite,
            @Value("${app.refresh-token.expiration-ms:604800000}") long expirationMs) {
        this.domain = domain;
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAge = Duration.ofMillis(expirationMs);
    }

    public ResponseCookie build(String value) {
        return base(value, maxAge).build();
    }

    public ResponseCookie clear() {
        return base("", Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value, Duration age) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(COOKIE_PATH)
                .maxAge(age);
        if (domain != null && !domain.isBlank()) {
            builder.domain(domain);
        }
        return builder;
    }
}
