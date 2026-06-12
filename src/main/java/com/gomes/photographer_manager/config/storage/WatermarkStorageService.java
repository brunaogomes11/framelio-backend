package com.gomes.photographer_manager.config.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class WatermarkStorageService {

    private static final String[] SUPPORTED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".webp"};

    private final Path storageRoot;

    public WatermarkStorageService(@Value("${storage.local.path:uploads/}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).resolve("watermarks").toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível criar diretório de marcas d'água", e);
        }
    }

    /**
     * Persiste a marca d'água do usuário sobrescrevendo qualquer arquivo anterior
     * e retorna a chave de armazenamento relativa (ex.: watermarks/{userId}.png).
     */
    public String save(String userId, MultipartFile file) throws IOException {
        deleteExisting(userId);
        String extension = extractExtension(file.getOriginalFilename());
        String filename = userId + extension;
        Path target = storageRoot.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "watermarks/" + filename;
    }

    public void delete(String userId) throws IOException {
        deleteExisting(userId);
    }

    private void deleteExisting(String userId) throws IOException {
        for (String extension : SUPPORTED_EXTENSIONS) {
            Files.deleteIfExists(storageRoot.resolve(userId + extension));
        }
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".png";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        boolean supported = Stream.of(SUPPORTED_EXTENSIONS).anyMatch(extension::equals);
        return supported ? extension : ".png";
    }
}
