package com.gomes.photographer_manager.domain.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, String> {

    List<Sale> findByPhotographerIdOrderByCreatedAtDesc(String photographerId);

    List<Sale> findByPhotographerIdAndPaymentStatusOrderByCreatedAtDesc(String photographerId, PaymentStatus paymentStatus);

    List<Sale> findTop5ByPhotographerIdOrderByCreatedAtDesc(String photographerId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.photographerId = :photographerId")
    long countByPhotographerId(@Param("photographerId") String photographerId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s WHERE s.photographerId = :photographerId")
    BigDecimal sumAmountByPhotographerId(@Param("photographerId") String photographerId);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s "
            + "WHERE s.photographerId = :photographerId AND s.paymentStatus = :paymentStatus")
    BigDecimal sumAmountByPhotographerIdAndPaymentStatus(@Param("photographerId") String photographerId,
                                                         @Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Sale s "
            + "WHERE s.photographerId = :photographerId AND s.paymentStatus = :paymentStatus "
            + "AND s.createdAt >= :start AND s.createdAt < :end")
    BigDecimal sumAmountByPhotographerIdAndPaymentStatusBetween(@Param("photographerId") String photographerId,
                                                                @Param("paymentStatus") PaymentStatus paymentStatus,
                                                                @Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end);
}
