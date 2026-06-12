package com.gomes.photographer_manager.domain.usuario;

import com.gomes.photographer_manager.config.storage.WatermarkStorageService;
import com.gomes.photographer_manager.domain.usuario.request.PatchMeRequest;
import com.gomes.photographer_manager.domain.usuario.response.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserMeService {

    private final UserRepository userRepository;
    private final WatermarkStorageService watermarkStorageService;

    public UserMeService(UserRepository userRepository,
                         WatermarkStorageService watermarkStorageService) {
        this.userRepository = userRepository;
        this.watermarkStorageService = watermarkStorageService;
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse patchMe(String email, PatchMeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        if (request.name() != null && !request.name().isBlank()) user.setName(request.name());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.cpf() != null) user.setCpf(request.cpf());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.portfolioGradient() != null) user.setPortfolioGradient(request.portfolioGradient());
        if (request.categories() != null) user.setCategories(request.categories());
        return new UserResponse(userRepository.save(user));
    }

    @Transactional
    public void updateProfilePhoto(String userId, String url) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        user.setProfilePhoto(url);
        userRepository.save(user);
    }

    @Transactional
    public void updateWatermark(String userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        String key = watermarkStorageService.save(userId, file);
        user.setWatermarkPath(key);
        userRepository.save(user);
    }

    @Transactional
    public void removeWatermark(String userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        watermarkStorageService.delete(userId);
        user.setWatermarkPath(null);
        userRepository.save(user);
    }
}
