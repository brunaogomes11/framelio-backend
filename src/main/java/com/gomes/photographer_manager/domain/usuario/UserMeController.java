package com.gomes.photographer_manager.domain.usuario;

import com.gomes.photographer_manager.config.storage.ProfileStorageService;
import com.gomes.photographer_manager.domain.usuario.request.PatchMeRequest;
import com.gomes.photographer_manager.domain.usuario.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Tag(name = "Meu Perfil")
@RestController
@RequestMapping("/users/me")
public class UserMeController {

    private final UserMeService userMeService;
    private final ProfileStorageService storageService;

    public UserMeController(UserMeService userMeService, ProfileStorageService storageService) {
        this.userMeService = userMeService;
        this.storageService = storageService;
    }

    @Operation(summary = "Retorna perfil do usuário autenticado")
    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(userMeService.getMe(principal.getUsername()));
    }

    @Operation(summary = "Atualiza dados editáveis do perfil")
    @PatchMapping
    public ResponseEntity<UserResponse> patchMe(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PatchMeRequest request) {
        return ResponseEntity.ok(userMeService.patchMe(principal.getUsername(), request));
    }

    @Operation(summary = "Faz upload da foto de perfil")
    @PostMapping("/photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("photo") MultipartFile photo) {
        User user = (User) principal;
        String url = storageService.store(user.getId(), photo);
        userMeService.updateProfilePhoto(user.getId(), url);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(summary = "Faz upload da marca d'água do fotógrafo")
    @PostMapping("/watermark")
    public ResponseEntity<Void> uploadWatermark(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("file") MultipartFile file) throws IOException {
        User user = (User) principal;
        userMeService.updateWatermark(user.getId(), file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a marca d'água do fotógrafo")
    @DeleteMapping("/watermark")
    public ResponseEntity<Void> removeWatermark(@AuthenticationPrincipal UserDetails principal) throws IOException {
        User user = (User) principal;
        userMeService.removeWatermark(user.getId());
        return ResponseEntity.noContent().build();
    }
}
