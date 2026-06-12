package com.gomes.photographer_manager.config.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ProfileStorageService {

    private final Path storageRoot;

    public ProfileStorageService(@Value("${storage.local.path:uploads/}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).resolve("profiles").toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar diretório de perfis", e);
        }
    }

    public String store(String userId, MultipartFile file) {
        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = userId + extension;
            Path target = storageRoot.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/files/profiles/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar foto de perfil", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}
