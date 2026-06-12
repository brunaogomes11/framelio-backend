package com.gomes.photographer_manager.domain.sale;

import com.gomes.photographer_manager.domain.sale.request.SaleRequest;
import com.gomes.photographer_manager.domain.sale.response.SaleResponse;
import com.gomes.photographer_manager.domain.sale.response.SaleStatsResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository repository;

    public SaleService(SaleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SaleResponse create(SaleRequest request, String photographerId) {
        Sale sale = new Sale(request, photographerId);
        return new SaleResponse(repository.save(sale));
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll(String photographerId, PaymentStatus status) {
        List<Sale> sales = status != null
                ? repository.findByPhotographerIdAndPaymentStatusOrderByCreatedAtDesc(photographerId, status)
                : repository.findByPhotographerIdOrderByCreatedAtDesc(photographerId);

        return sales.stream()
                .map(SaleResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse findById(String id) {
        return new SaleResponse(findEntity(id));
    }

    @Transactional
    public SaleResponse update(String id, SaleRequest request, String photographerId) {
        Sale sale = findEntity(id);
        validateOwnership(sale, photographerId);
        sale.update(request);
        return new SaleResponse(repository.save(sale));
    }

    @Transactional
    public void delete(String id, String photographerId) {
        Sale sale = findEntity(id);
        validateOwnership(sale, photographerId);
        repository.delete(sale);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findRecent(String photographerId) {
        return repository.findTop5ByPhotographerIdOrderByCreatedAtDesc(photographerId)
                .stream()
                .map(SaleResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleStatsResponse getStats(String photographerId) {
        return new SaleStatsResponse(
                repository.sumAmountByPhotographerId(photographerId),
                repository.sumAmountByPhotographerIdAndPaymentStatus(photographerId, PaymentStatus.PAID),
                repository.sumAmountByPhotographerIdAndPaymentStatus(photographerId, PaymentStatus.PENDING),
                repository.sumAmountByPhotographerIdAndPaymentStatus(photographerId, PaymentStatus.OVERDUE),
                (int) repository.countByPhotographerId(photographerId)
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal revenueThisMonth(String photographerId, LocalDate month) {
        LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return repository.sumAmountByPhotographerIdAndPaymentStatusBetween(
                photographerId, PaymentStatus.PAID, start, end);
    }

    private Sale findEntity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sale not found with id: " + id));
    }

    private void validateOwnership(Sale sale, String photographerId) {
        if (!sale.getPhotographerId().equals(photographerId)) {
            throw new DataIntegrityViolationException("A venda não pertence ao fotógrafo autenticado");
        }
    }
}
