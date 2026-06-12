package com.gomes.photographer_manager.domain.store;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomes.photographer_manager.config.email.EmailService;
import com.gomes.photographer_manager.config.storage.StorageService;
import com.gomes.photographer_manager.domain.gallery.Gallery;
import com.gomes.photographer_manager.domain.gallery.GalleryRepository;
import com.gomes.photographer_manager.domain.gallery.GalleryResponse;
import com.gomes.photographer_manager.domain.gallery.Photo;
import com.gomes.photographer_manager.domain.gallery.PhotoRepository;
import com.gomes.photographer_manager.domain.store.balance.BalanceRepository;
import com.gomes.photographer_manager.domain.store.balance.BalanceResponse;
import com.gomes.photographer_manager.domain.store.balance.PhotographerBalance;
import com.gomes.photographer_manager.domain.store.download.DownloadToken;
import com.gomes.photographer_manager.domain.store.download.DownloadTokenRepository;
import com.gomes.photographer_manager.domain.store.order.CheckoutRequest;
import com.gomes.photographer_manager.domain.store.order.CheckoutResponse;
import com.gomes.photographer_manager.domain.store.order.Order;
import com.gomes.photographer_manager.domain.store.order.OrderRepository;
import com.gomes.photographer_manager.domain.store.order.OrderResponse;
import com.gomes.photographer_manager.domain.store.withdrawal.WithdrawalPayload;
import com.gomes.photographer_manager.domain.store.withdrawal.WithdrawalRepository;
import com.gomes.photographer_manager.domain.store.withdrawal.WithdrawalRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

@Service
public class StoreService {

    private final GalleryRepository galleryRepository;
    private final PhotoRepository photoRepository;
    private final OrderRepository orderRepository;
    private final DownloadTokenRepository downloadTokenRepository;
    private final BalanceRepository balanceRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final StorageService storageService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${store.commission-rate}")
    private double commissionRate;

    @Value("${store.download-expiry-hours}")
    private int downloadExpiryHours;

    @Value("${store.max-downloads}")
    private int maxDownloads;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    public StoreService(GalleryRepository galleryRepository, PhotoRepository photoRepository,
                        OrderRepository orderRepository, DownloadTokenRepository downloadTokenRepository,
                        BalanceRepository balanceRepository, WithdrawalRepository withdrawalRepository,
                        StorageService storageService, EmailService emailService) {
        this.galleryRepository = galleryRepository;
        this.photoRepository = photoRepository;
        this.orderRepository = orderRepository;
        this.downloadTokenRepository = downloadTokenRepository;
        this.balanceRepository = balanceRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.storageService = storageService;
        this.emailService = emailService;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newHttpClient();
    }

    @Transactional
    public GalleryResponse enableStore(String galleryId, BigDecimal pricePerPhoto,
                                       BigDecimal priceFullAlbum, String photographerId) {
        Gallery gallery = galleryRepository.findById(galleryId)
            .orElseThrow(() -> new EntityNotFoundException("Galeria nao encontrada"));
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Galeria nao pertence ao fotografo autenticado");
        }
        if (pricePerPhoto == null && priceFullAlbum == null) {
            throw new IllegalArgumentException("Informe ao menos um preco");
        }

        gallery.setPricePerPhoto(pricePerPhoto);
        gallery.setPriceFullAlbum(priceFullAlbum);
        gallery.setStoreEnabled(true);
        if (gallery.getShareToken() == null) {
            gallery.setShareToken(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        }

        Gallery saved = galleryRepository.save(gallery);
        long photoCount = photoRepository.countByGalleryId(saved.getId());
        Photo cover = photoRepository.findFirstByGalleryIdOrderByDisplayOrderAsc(saved.getId()).orElse(null);
        String coverUrl = cover != null ? storageService.getUrl(cover.getStoragePath()) : null;
        return new GalleryResponse(saved, photoCount, coverUrl, null);
    }

    @Transactional(readOnly = true)
    public PublicGalleryResponse getPublicGallery(String shareToken) {
        Gallery gallery = galleryRepository.findByShareTokenAndStoreEnabledTrue(shareToken)
            .orElseThrow(() -> new EntityNotFoundException("Galeria nao encontrada ou nao disponivel para venda"));
        List<Photo> photos = photoRepository.findByGalleryIdOrderByDisplayOrderAsc(gallery.getId());
        List<PublicPhotoResponse> photoResponses = photos.stream()
            .map(p -> new PublicPhotoResponse(p.getId(), storageService.getUrl(p.getStoragePath())))
            .collect(Collectors.toList());
        return new PublicGalleryResponse(gallery.getId(), gallery.getTitle(), gallery.getDescription(),
            gallery.getPricePerPhoto(), gallery.getPriceFullAlbum(), photoResponses);
    }

    @Transactional
    public CheckoutResponse createCheckout(String shareToken, CheckoutRequest request) {
        Gallery gallery = galleryRepository.findByShareTokenAndStoreEnabledTrue(shareToken)
            .orElseThrow(() -> new EntityNotFoundException("Galeria nao encontrada"));

        List<Photo> orderPhotos = resolveOrderPhotos(gallery, request);
        BigDecimal subtotal = calculateSubtotal(gallery, request, orderPhotos.size());
        BigDecimal platformFee = subtotal.multiply(BigDecimal.valueOf(commissionRate)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal photographerAmount = subtotal.subtract(platformFee);

        Order order = new Order();
        order.setGalleryId(gallery.getId());
        order.setPhotographerId(gallery.getPhotographerId());
        order.setBuyerName(request.buyerName());
        order.setBuyerEmail(request.buyerEmail());
        order.setBuyerCpf(onlyDigits(request.buyerCpf()));
        order.setOrderType(request.orderType());
        order.setPhotoIds(orderPhotos.stream().map(Photo::getId).collect(Collectors.joining(",")));
        order.setSubtotal(subtotal);
        order.setPlatformFee(platformFee);
        order.setPhotographerAmount(photographerAmount);
        order = orderRepository.save(order);

        try {
            JsonNode mpOrder = createMercadoPagoOrder(order, gallery, request, subtotal, orderPhotos.size());
            JsonNode payment = firstPayment(mpOrder);
            JsonNode paymentMethod = payment.path("payment_method");

            order.setMpOrderId(text(mpOrder, "id"));
            order.setMpPaymentId(text(payment, "id"));
            order.setMpPaymentUrl(text(paymentMethod, "ticket_url"));
            orderRepository.save(order);

            DownloadToken downloadToken = isPaid(mpOrder, payment) ? markOrderPaid(order) : null;

            return new CheckoutResponse(
                    order.getId(),
                    order.getMpOrderId(),
                    order.getMpPaymentId(),
                    text(mpOrder, "status"),
                    text(mpOrder, "status_detail"),
                    request.paymentMethod().trim().toUpperCase(),
                    text(paymentMethod, "ticket_url"),
                    text(paymentMethod, "qr_code"),
                    text(paymentMethod, "qr_code_base64"),
                    downloadToken == null ? null : downloadToken.getToken()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar pagamento: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processOrderWebhook(String mercadoPagoOrderId) {
        try {
            JsonNode mpOrder = getMercadoPagoOrder(mercadoPagoOrderId);
            JsonNode payment = firstPayment(mpOrder);
            Order order = orderRepository.findByMpOrderId(mercadoPagoOrderId)
                    .or(() -> orderRepository.findById(text(mpOrder, "external_reference")))
                    .orElse(null);
            if (order == null) {
                return;
            }
            order.setMpOrderId(text(mpOrder, "id"));
            order.setMpPaymentId(text(payment, "id"));
            orderRepository.save(order);
            if (isPaid(mpOrder, payment)) {
                markOrderPaid(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar webhook da order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void processPaymentWebhook(String paymentId) {
        Order order = orderRepository.findByMpPaymentId(paymentId).orElse(null);
        if (order != null && order.getMpOrderId() != null) {
            processOrderWebhook(order.getMpOrderId());
            return;
        }

        try {
            com.mercadopago.client.payment.PaymentClient paymentClient = new com.mercadopago.client.payment.PaymentClient();
            com.mercadopago.resources.payment.Payment payment = paymentClient.get(Long.parseLong(paymentId));
            if (!"approved".equals(payment.getStatus())) {
                return;
            }
            String orderId = payment.getExternalReference();
            order = orderRepository.findById(orderId)
                .or(() -> orderRepository.findByMpPaymentId(paymentId))
                .orElse(null);
            if (order != null) {
                order.setMpPaymentId(paymentId);
                markOrderPaid(order);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar webhook: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<String> getDownloadUrls(String token) {
        DownloadToken dt = downloadTokenRepository.findByToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Link de download invalido"));
        if (dt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Link de download expirado");
        }
        if (dt.getDownloadCount() >= dt.getMaxDownloads()) {
            throw new IllegalStateException("Limite de downloads atingido");
        }

        List<String> photoIds = Arrays.asList(dt.getPhotoIds().split(","));
        return photoIds.stream()
            .map(id -> photoRepository.findById(id.trim()).orElse(null))
            .filter(p -> p != null)
            .map(p -> storageService.getUrl(p.getStoragePath()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void writeDownloadZip(String token, OutputStream outputStream) {
        DownloadToken dt = downloadTokenRepository.findByToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Link de download invalido"));
        if (dt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Link de download expirado");
        }
        if (dt.getDownloadCount() >= dt.getMaxDownloads()) {
            throw new IllegalStateException("Limite de downloads atingido");
        }

        List<Photo> photos = Arrays.stream(dt.getPhotoIds().split(","))
            .map(id -> photoRepository.findById(id.trim()).orElse(null))
            .filter(p -> p != null)
            .toList();

        try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
            for (int i = 0; i < photos.size(); i++) {
                Photo photo = photos.get(i);
                zip.putNextEntry(new ZipEntry("foto-" + (i + 1) + extensionFrom(photo.getStoragePath())));
                storageService.writeTo(photo.getStoragePath(), zip);
                zip.closeEntry();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar ZIP: " + e.getMessage(), e);
        }

        dt.setDownloadCount(dt.getDownloadCount() + 1);
        downloadTokenRepository.save(dt);
    }

    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String photographerId) {
        return balanceRepository.findByPhotographerId(photographerId)
            .map(b -> new BalanceResponse(b.getAvailableAmount(), b.getPendingAmount(), b.getTotalEarned()))
            .orElse(new BalanceResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Transactional
    public void requestWithdrawal(String photographerId, WithdrawalPayload payload) {
        PhotographerBalance balance = balanceRepository.findByPhotographerId(photographerId)
            .orElseThrow(() -> new IllegalStateException("Saldo nao encontrado"));
        if (payload.amount().compareTo(balance.getAvailableAmount()) > 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        balance.setAvailableAmount(balance.getAvailableAmount().subtract(payload.amount()));
        balanceRepository.save(balance);

        WithdrawalRequest wr = new WithdrawalRequest();
        wr.setPhotographerId(photographerId);
        wr.setAmount(payload.amount());
        wr.setPixKey(payload.pixKey());
        wr.setPixKeyType(payload.pixKeyType());
        withdrawalRepository.save(wr);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String photographerId) {
        return orderRepository.findByPhotographerIdOrderByCreatedAtDesc(photographerId)
            .stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getGalleryOrders(String galleryId, String photographerId) {
        Gallery gallery = galleryRepository.findById(galleryId)
            .orElseThrow(() -> new EntityNotFoundException("Galeria nao encontrada"));
        if (!gallery.getPhotographerId().equals(photographerId)) {
            throw new IllegalArgumentException("Galeria nao pertence ao fotografo");
        }
        return orderRepository.findByGalleryIdOrderByCreatedAtDesc(galleryId)
            .stream().map(OrderResponse::new).collect(Collectors.toList());
    }

    private List<Photo> resolveOrderPhotos(Gallery gallery, CheckoutRequest request) {
        if ("FULL_ALBUM".equals(request.orderType())) {
            if (gallery.getPriceFullAlbum() == null) {
                throw new IllegalArgumentException("Esta galeria nao tem preco de album completo definido");
            }
            List<Photo> photos = photoRepository.findByGalleryIdOrderByDisplayOrderAsc(gallery.getId());
            if (photos.isEmpty()) {
                throw new IllegalArgumentException("Esta galeria nao possui fotos disponiveis");
            }
            return photos;
        }

        if (!"INDIVIDUAL".equals(request.orderType())) {
            throw new IllegalArgumentException("Tipo de pedido invalido");
        }
        if (gallery.getPricePerPhoto() == null) {
            throw new IllegalArgumentException("Esta galeria nao tem preco por foto definido");
        }
        if (request.photoIds() == null || request.photoIds().isEmpty()) {
            throw new IllegalArgumentException("Selecione ao menos uma foto");
        }

        List<String> requestedIds = request.photoIds().stream().distinct().toList();
        List<Photo> photos = photoRepository.findAllById(requestedIds);
        boolean allPhotosBelongToGallery = photos.size() == requestedIds.size()
                && photos.stream().allMatch(photo -> gallery.getId().equals(photo.getGalleryId()));
        if (!allPhotosBelongToGallery) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uma ou mais fotos nao pertencem a esta galeria");
        }
        return photos;
    }

    private BigDecimal calculateSubtotal(Gallery gallery, CheckoutRequest request, int photoCount) {
        if ("FULL_ALBUM".equals(request.orderType())) {
            return gallery.getPriceFullAlbum();
        }
        return gallery.getPricePerPhoto().multiply(BigDecimal.valueOf(photoCount));
    }

    private JsonNode createMercadoPagoOrder(Order order, Gallery gallery, CheckoutRequest request,
                                            BigDecimal subtotal, int photoCount)
            throws IOException, InterruptedException {
        Map<String, Object> paymentMethod = buildPaymentMethod(request);
        Map<String, Object> payment = new LinkedHashMap<>();
        payment.put("amount", money(subtotal));
        payment.put("payment_method", paymentMethod);
        if ("PIX".equals(request.paymentMethod().trim().toUpperCase())) {
            payment.put("expiration_time", "PT30M");
        }

        Map<String, Object> payer = new LinkedHashMap<>();
        payer.put("email", request.buyerEmail());
        payer.put("first_name", firstName(request.buyerName()));
        payer.put("last_name", lastName(request.buyerName()));
        if (!isBlank(request.buyerCpf())) {
            payer.put("identification", Map.of("type", "CPF", "number", onlyDigits(request.buyerCpf())));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "online");
        body.put("processing_mode", "automatic");
        body.put("capture_mode", "automatic");
        body.put("external_reference", order.getId());
        body.put("total_amount", money(subtotal));
        body.put("payer", payer);
        body.put("transactions", Map.of("payments", List.of(payment)));
        body.put("description", "Fotos - " + gallery.getTitle());
        body.put("items", List.of(Map.of(
                "title", gallery.getTitle(),
                "unit_price", money(subtotal),
                "quantity", 1,
                "description", photoCount == 1 ? "1 foto digital" : photoCount + " fotos digitais",
                "external_code", order.getId()
        )));

        return sendMercadoPago("POST", "https://api.mercadopago.com/v1/orders", body);
    }

    private Map<String, Object> buildPaymentMethod(CheckoutRequest request) {
        String method = request.paymentMethod().trim().toUpperCase();
        Map<String, Object> paymentMethod = new LinkedHashMap<>();
        if ("PIX".equals(method)) {
            paymentMethod.put("id", "pix");
            paymentMethod.put("type", "bank_transfer");
            return paymentMethod;
        }
        if ("CARD".equals(method)) {
            if (isBlank(request.cardToken()) || isBlank(request.paymentMethodId()) || isBlank(request.paymentType())) {
                throw new IllegalArgumentException("Dados do cartao incompletos");
            }
            paymentMethod.put("id", request.paymentMethodId());
            paymentMethod.put("type", request.paymentType());
            paymentMethod.put("token", request.cardToken());
            paymentMethod.put("installments", request.installments() == null ? 1 : request.installments());
            return paymentMethod;
        }
        throw new IllegalArgumentException("Meio de pagamento invalido");
    }

    private JsonNode getMercadoPagoOrder(String mercadoPagoOrderId) throws IOException, InterruptedException {
        return sendMercadoPago("GET", "https://api.mercadopago.com/v1/orders/" + mercadoPagoOrderId, null);
    }

    private JsonNode sendMercadoPago(String method, String url, Object body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + mercadoPagoAccessToken);

        if ("POST".equals(method)) {
            builder.header("X-Idempotency-Key", UUID.randomUUID().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        } else {
            builder.GET();
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(formatMercadoPagoError(response.statusCode(), response.body()));
        }
        return objectMapper.readTree(response.body());
    }

    private String formatMercadoPagoError(int statusCode, String body) {
        String fallback = "Mercado Pago retornou " + statusCode + ": " + body;
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                JsonNode first = errors.get(0);
                String code = text(first, "code");
                String message = text(first, "message");
                String details = first.path("details").isArray()
                        ? first.path("details").findValuesAsText("").stream().collect(Collectors.joining(" | "))
                        : null;
                if ("property_value".equals(code) && details != null && details.contains("payment_method.type")) {
                    return "Pix indisponivel para esta conta/aplicacao do Mercado Pago. Cadastre uma chave Pix na conta vendedora e use credenciais de teste/producao compatíveis. Detalhes: " + details;
                }
                return "Mercado Pago retornou " + statusCode + ": " + List.of(code, message, details).stream()
                        .filter(value -> value != null && !value.isBlank())
                        .collect(Collectors.joining(" - "));
            }
        } catch (Exception ignored) {
            // fall through to raw response
        }
        return fallback;
    }

    private DownloadToken markOrderPaid(Order order) {
        if ("PAID".equals(order.getStatus())) {
            return downloadTokenRepository.findByOrderId(order.getId()).orElse(null);
        }

        order.setStatus("PAID");
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);

        PhotographerBalance balance = balanceRepository.findByPhotographerId(order.getPhotographerId())
            .orElseGet(() -> {
                PhotographerBalance b = new PhotographerBalance();
                b.setPhotographerId(order.getPhotographerId());
                return b;
            });
        balance.setAvailableAmount(balance.getAvailableAmount().add(order.getPhotographerAmount()));
        balance.setTotalEarned(balance.getTotalEarned().add(order.getPhotographerAmount()));
        balanceRepository.save(balance);

        DownloadToken dt = downloadTokenRepository.findByOrderId(order.getId()).orElseGet(DownloadToken::new);
        dt.setOrderId(order.getId());
        dt.setBuyerEmail(order.getBuyerEmail());
        dt.setPhotoIds(order.getPhotoIds());
        dt.setExpiresAt(LocalDateTime.now().plusHours(downloadExpiryHours));
        dt.setMaxDownloads(maxDownloads);
        dt = downloadTokenRepository.save(dt);

        String downloadLink = frontendUrl + "/download/" + dt.getToken();
        emailService.sendDownloadEmail(order.getBuyerEmail(), order.getBuyerName(), downloadLink);
        return dt;
    }

    private String extensionFrom(String path) {
        int dotIndex = path == null ? -1 : path.lastIndexOf('.');
        if (dotIndex < 0) {
            return ".jpg";
        }
        return path.substring(dotIndex);
    }

    private boolean isPaid(JsonNode mpOrder, JsonNode payment) {
        String orderStatus = text(mpOrder, "status");
        String orderDetail = text(mpOrder, "status_detail");
        String paymentStatus = text(payment, "status");
        String paymentDetail = text(payment, "status_detail");
        return "processed".equals(orderStatus)
                || "accredited".equals(orderDetail)
                || "processed".equals(paymentStatus)
                || "accredited".equals(paymentDetail);
    }

    private JsonNode firstPayment(JsonNode mpOrder) {
        return mpOrder.path("transactions").path("payments").path(0);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String firstName(String fullName) {
        if (isBlank(fullName)) {
            return "";
        }
        return fullName.trim().split("\\s+")[0];
    }

    private String lastName(String fullName) {
        if (isBlank(fullName)) {
            return "";
        }
        String trimmed = fullName.trim();
        int firstSpace = trimmed.indexOf(' ');
        return firstSpace < 0 ? "" : trimmed.substring(firstSpace + 1);
    }

    private String onlyDigits(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
