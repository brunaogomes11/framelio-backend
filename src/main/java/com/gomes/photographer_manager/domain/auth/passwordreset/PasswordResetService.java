package com.gomes.photographer_manager.domain.auth.passwordreset;

import com.gomes.photographer_manager.config.email.EmailService;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public void createToken(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }
        tokenRepository.deleteByUserId(user.get().getId());
        PasswordResetToken token = tokenRepository.save(new PasswordResetToken(user.get().getId()));
        String resetLink = frontendUrl + "/reset-password?token=" + token.getToken();
        emailService.sendPasswordResetEmail(email, resetLink);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token já utilizado");
        }
        if (!resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
