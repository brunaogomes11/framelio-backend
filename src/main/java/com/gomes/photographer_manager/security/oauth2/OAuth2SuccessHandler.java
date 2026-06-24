package com.gomes.photographer_manager.security.oauth2;

import com.gomes.photographer_manager.domain.auth.refresh.RefreshTokenCookieFactory;
import com.gomes.photographer_manager.domain.auth.refresh.RefreshTokenService;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenCookieFactory cookieFactory;
    private final String frontendRedirectUri;

    public OAuth2SuccessHandler(JwtService jwtService,
                                RefreshTokenService refreshTokenService,
                                RefreshTokenCookieFactory cookieFactory,
                                @Value("${app.oauth2.redirect-uri}") String frontendRedirectUri) {
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.cookieFactory = cookieFactory;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        String token = jwtService.generateToken(user);

        String refreshToken = refreshTokenService.issue(user.getId());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieFactory.build(refreshToken).toString());

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
