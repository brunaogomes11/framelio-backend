package com.gomes.photographer_manager.domain.auth;

import com.gomes.photographer_manager.domain.auth.request.RegisterRequest;
import com.gomes.photographer_manager.enums.ProfileEnum;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gomes.photographer_manager.domain.auth.request.LoginRequest;
import com.gomes.photographer_manager.domain.auth.response.AuthResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import com.gomes.photographer_manager.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.profile() == ProfileEnum.ADMIN) {
            throw new IllegalArgumentException("Perfil ADMIN não pode ser registrado.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        if (request.cpf() != null && !request.cpf().isBlank() && userRepository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        User user = new User(request, passwordEncoder.encode(request.password()));
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
