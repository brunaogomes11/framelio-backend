package com.gomes.photographer_manager.config.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    @Override
    public String store(MultipartFile file, String prefix) {
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public String getUrl(String key) {
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }

    @Override
    public void writeTo(String key, OutputStream outputStream) {
        throw new UnsupportedOperationException("S3 storage not implemented yet");
    }
}
