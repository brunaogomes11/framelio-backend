package com.gomes.photographer_manager.domain.usuario;

import com.gomes.photographer_manager.enums.ProfileEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> findByProfile(ProfileEnum profile);

}
