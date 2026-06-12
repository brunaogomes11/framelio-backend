package com.gomes.photographer_manager.domain.usuario;

import com.gomes.photographer_manager.domain.usuario.request.UserRequest;
import com.gomes.photographer_manager.domain.usuario.response.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.cpf() != null && repository.existsByCpf(request.cpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email inválido");
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        var user = new User(request, encodedPassword);
        return new UserResponse(repository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        return new UserResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listAll() {
        return repository.findAll().stream()
                .map(UserResponse::new)
                .toList();
    }

    @Transactional
    public UserResponse update(String id, UserRequest request) {
        var user = findEntity(id);

        String encodedPassword = null;
        if (request.password() != null && !request.password().isBlank()) {
            encodedPassword = passwordEncoder.encode(request.password());
        }

        user.update(request, encodedPassword);
        return new UserResponse(repository.save(user));
    }

    @Transactional
    public void delete(String id) {
        var user = findEntity(id);
        repository.delete(user);
    }

    private User findEntity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }
}
