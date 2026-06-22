package com.gomes.photographer_manager.domain.gallery;

import com.gomes.photographer_manager.config.storage.StorageService;
import com.gomes.photographer_manager.config.storage.WatermarkService;
import com.gomes.photographer_manager.domain.usuario.User;
import com.gomes.photographer_manager.domain.usuario.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private GalleryService galleryService;

    @Mock
    private StorageService storageService;

    @Mock
    private WatermarkService watermarkService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PhotoService photoService;

    private static final byte[] ORIGINAL_BYTES = new byte[]{1, 2, 3};
    private static final byte[] WATERMARK_BYTES = new byte[]{4, 5, 6};
    private static final byte[] WATERMARKED_BYTES = new byte[]{7, 8, 9};

    private Photo photo(String galleryId) {
        Photo photo = new Photo(galleryId, "galleries/g1/file.jpg", "caption", 0);
        photo.setId("photo1");
        return photo;
    }

    private Gallery gallery(String photographerId, boolean storeEnabled, boolean shareEnabled) {
        Gallery gallery = new Gallery(photographerId, null, "Title", "Desc", true);
        gallery.setStoreEnabled(storeEnabled);
        gallery.setShareEnabled(shareEnabled);
        return gallery;
    }

    private void stubStorageReturns(String key, byte[] content) throws IOException {
        doAnswer(invocation -> {
            OutputStream out = invocation.getArgument(1);
            out.write(content);
            return null;
        }).when(storageService).writeTo(eq(key), any(OutputStream.class));
    }

    @Test
    void viewServesWatermarkedWhenStoreEnabledAndAnonymous() throws IOException {
        Gallery gallery = gallery("owner1", true, false);
        when(photoRepository.findById("photo1")).thenReturn(Optional.of(photo("g1")));
        when(galleryService.getEntity("g1")).thenReturn(gallery);

        User photographer = new User();
        photographer.setWatermarkPath("watermarks/owner1.png");
        when(userRepository.findById("owner1")).thenReturn(Optional.of(photographer));

        stubStorageReturns("galleries/g1/file.jpg", ORIGINAL_BYTES);
        stubStorageReturns("watermarks/owner1.png", WATERMARK_BYTES);
        when(watermarkService.applyWatermark(ORIGINAL_BYTES, WATERMARK_BYTES, "galleries/g1/file.jpg"))
                .thenReturn(WATERMARKED_BYTES);

        PhotoView view = photoService.view("photo1", null);

        assertArrayEquals(WATERMARKED_BYTES, view.content());
        assertEquals("image/jpeg", view.contentType());
    }

    @Test
    void viewServesOriginalToGalleryOwner() throws IOException {
        Gallery gallery = gallery("owner1", true, false);
        when(photoRepository.findById("photo1")).thenReturn(Optional.of(photo("g1")));
        when(galleryService.getEntity("g1")).thenReturn(gallery);
        stubStorageReturns("galleries/g1/file.jpg", ORIGINAL_BYTES);

        PhotoView view = photoService.view("photo1", "owner1");

        assertArrayEquals(ORIGINAL_BYTES, view.content());
        verify(watermarkService, never()).applyWatermark(any(), any(), anyString());
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void viewServesOriginalWhenShareEnabled() throws IOException {
        Gallery gallery = gallery("owner1", true, true);
        when(photoRepository.findById("photo1")).thenReturn(Optional.of(photo("g1")));
        when(galleryService.getEntity("g1")).thenReturn(gallery);
        stubStorageReturns("galleries/g1/file.jpg", ORIGINAL_BYTES);

        PhotoView view = photoService.view("photo1", null);

        assertArrayEquals(ORIGINAL_BYTES, view.content());
        verify(watermarkService, never()).applyWatermark(any(), any(), anyString());
    }

    @Test
    void viewThrowsWhenPhotoNotFound() {
        when(photoRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> photoService.view("missing", null));
    }

    @Test
    void uploadReturnsUrlUsingCurrentRequestHost() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("https");
        request.setServerName("vps.example.com");
        request.setServerPort(443);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            Gallery gallery = gallery("owner1", true, false);
            when(galleryService.getEntity("g1")).thenReturn(gallery);
            when(storageService.store(any(), anyString())).thenReturn("galleries/g1/file.jpg");
            when(photoRepository.save(any(Photo.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PhotoResponse response = photoService.upload("g1", any(), "caption", "owner1");

            assertEquals("https://vps.example.com/files/galleries/g1/file.jpg", response.url());
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
