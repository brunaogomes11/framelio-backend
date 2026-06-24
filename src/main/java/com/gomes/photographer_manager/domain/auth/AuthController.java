package com.gomes.photographer_manager.domain.auth;

import com.gomes.photographer_manager.domain.auth.passwordreset.PasswordResetService;
import com.gomes.photographer_manager.domain.auth.refresh.RefreshTokenCookieFactory;
import com.gomes.photographer_manager.domain.auth.request.LoginRequest;
import com.gomes.photographer_manager.domain.auth.request.RegisterRequest;
import com.gomes.photographer_manager.domain.auth.response.AuthResponse;
import com.gomes.photographer_manager.domain.auth.response.AuthTokens;
import com.gomes.photographer_manager.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação JWT")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenCookieFactory cookieFactory;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          RefreshTokenCookieFactory cookieFactory) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.cookieFactory = cookieFactory;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login com e-mail e senha",
            description = "Autentica o usuário e retorna um access token (JWT) no corpo e um refresh token em cookie httpOnly."
    )
    @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return respondWithTokens(authService.login(request), HttpStatus.OK);
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registro de novo usuário",
            description = "Cria uma conta com perfil CLIENT ou PHOTOGRAPHER. Retorna access token no corpo e refresh token em cookie httpOnly. O perfil ADMIN não é permitido nesta rota."
    )
    @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou perfil ADMIN informado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return respondWithTokens(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renova o access token",
            description = "Usa o refresh token do cookie httpOnly para emitir um novo access token e rotacionar o refresh token."
    )
    @ApiResponse(responseCode = "200", description = "Token renovado",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token ausente, inválido ou expirado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return respondWithTokens(authService.refresh(refreshToken), HttpStatus.OK);
        } catch (RuntimeException ex) {
            // Token inválido/reutilizado/expirado → limpa o cookie e responde 401.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                    .build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoga o refresh token e limpa o cookie.")
    @ApiResponse(responseCode = "204", description = "Logout efetuado")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookieFactory.COOKIE_NAME, required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clear().toString())
                .build();
    }

    private ResponseEntity<AuthResponse> respondWithTokens(AuthTokens tokens, HttpStatus status) {
        ResponseCookie cookie = cookieFactory.build(tokens.refreshToken());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar redefinição de senha",
            description = "Envia um e-mail com link de redefinição de senha válido por 1 hora. Não retorna erro se o e-mail não existir (segurança).")
    @ApiResponse(responseCode = "204", description = "Solicitação processada")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createToken(request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Redefinir senha com token",
            description = "Redefine a senha do usuário usando o token recebido por e-mail.")
    @ApiResponse(responseCode = "204", description = "Senha redefinida com sucesso")
    @ApiResponse(responseCode = "400", description = "Token inválido, expirado ou já utilizado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
