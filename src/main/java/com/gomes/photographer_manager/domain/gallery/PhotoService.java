package com.gomes.photographer_manager.domain.gallery;

import com.gomes.photographer_manager.config.storage.StorageService;
import com.gomes.photographer_manager.config.storage.WatermarkService;
import com.gomes.photographer_manager.domain.gallery.request.PhotoOrderItem;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final GalleryService galleryService;
    private final StorageService storageService;
    private final WatermarkService watermarkService;
    private final UserRepository userRepository;

    public PhotoService(PhotoRepository photoRepository,
                        GalleryService galleryService,
                        StorageService storageService,
                        WatermarkService watermarkService,
                        UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.galleryService = galleryService;
        this.storageService = storageService;
        this.watermarkService = watermarkService;
        this.userRepository = userRepository;
    }

    @Transactional
    public PhotoResponse upload(String galleryId, MultipartFile file, String caption, String photographerId) {
        Gallery gallery = galleryService.getEntity(galleryId);
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Galeria não pertence ao fotógrafo autenticado");
        }
        String key;
        try {
            key = storageService.store(file, "galleries/" + galleryId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Photo photo = photoRepository.save(new Photo(galleryId, key, caption, 0));
        return new PhotoResponse(photo, storageService.getUrl(photo.getStoragePath()));
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> findByGallery(String galleryId) {
        return photoRepository.findByGalleryIdOrderByDisplayOrderAsc(galleryId).stream()
                .map(photo -> new PhotoResponse(photo, storageService.getUrl(photo.getStoragePath())))
                .toList();
    }

    @Transactional
    public PhotoResponse togglePortfolio(String photoId, String photographerId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Foto não encontrada para ID: " + photoId));
        Gallery gallery = galleryService.getEntity(photo.getGalleryId());
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Foto não pertence ao fotógrafo autenticado");
        }
        photo.setPortfolio(!photo.isPortfolio());
        return new PhotoResponse(photoRepository.save(photo), storageService.getUrl(photo.getStoragePath()));
    }

    @Transactional
    public void reorderPortfolio(String photographerId, List<PhotoOrderItem> items) {
        for (PhotoOrderItem item : items) {
            Photo photo = photoRepository.findById(item.photoId())
                    .orElseThrow(() -> new EntityNotFoundException("Foto não encontrada: " + item.photoId()));
            Gallery gallery = galleryService.getEntity(photo.getGalleryId());
            if (!gallery.getPhotographerId().equals(photographerId)) {
                throw new IllegalArgumentException("Foto não pertence ao fotógrafo.");
            }
            photo.setPortfolioOrder(item.order());
            photoRepository.save(photo);
        }
    }

    @Transactional
    public PhotoResponse updateCaption(String photoId, String caption, String photographerId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Foto não encontrada"));
        Gallery gallery = galleryService.getEntity(photo.getGalleryId());
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Foto não pertence ao fotógrafo autenticado.");
        }
        photo.setCaption(caption);
        return new PhotoResponse(photoRepository.save(photo), storageService.getUrl(photo.getStoragePath()));
    }

    /**
     * Resolve os bytes da foto para visualização pública.
     * Serve o original quando o solicitante é o fotógrafo dono, quando a galeria não tem loja ativa
     * ou quando o compartilhamento por link está habilitado; caso contrário aplica a marca d'água do fotógrafo.
     *
     * @param requesterId id do usuário autenticado ou {@code null} para acesso anônimo
     */
    @Transactional(readOnly = true)
    public PhotoView view(String photoId, String requesterId) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Foto não encontrada para ID: " + photoId));
        Gallery gallery = galleryService.getEntity(photo.getGalleryId());

        String contentType = photo.getStoragePath().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        byte[] photoBytes = readKey(photo.getStoragePath());

        boolean ownerRequest = requesterId != null && gallery.getPhotographerId().equals(requesterId);
        boolean serveOriginal = ownerRequest || !gallery.isStoreEnabled() || gallery.isShareEnabled();
        if (serveOriginal) {
            return new PhotoView(photoBytes, contentType);
        }

        User photographer = userRepository.findById(gallery.getPhotographerId())
                .orElseThrow(() -> new EntityNotFoundException("Fotógrafo não encontrado"));
        String watermarkKey = photographer.getWatermarkPath();
        if (watermarkKey == null) {
            return new PhotoView(photoBytes, contentType);
        }

        byte[] watermarkBytes;
        try {
            watermarkBytes = readKey(watermarkKey);
        } catch (IOException e) {
            return new PhotoView(photoBytes, contentType);
        }

        byte[] watermarked = watermarkService.applyWatermark(photoBytes, watermarkBytes, photo.getStoragePath());
        return new PhotoView(watermarked, contentType);
    }

    private byte[] readKey(String key) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        storageService.writeTo(key, output);
        return output.toByteArray();
    }

    @Transactional
    public void delete(String id, String photographerId) {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Foto não encontrada para ID: " + id));
        Gallery gallery = galleryService.getEntity(photo.getGalleryId());
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Foto não pertence ao fotógrafo autenticado");
        }
        storageService.delete(photo.getStoragePath());
        photoRepository.delete(photo);
    }
}
