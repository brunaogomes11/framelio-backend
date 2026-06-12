package com.gomes.photographer_manager.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {

    List<Event> findByPhotographerIdOrderByDateTimeAsc(String photographerId);

    List<Event> findByPhotographerIdAndStatusOrderByDateTimeAsc(String photographerId, EventStatus status);

    List<Event> findTop5ByPhotographerIdAndDateTimeAfterOrderByDateTimeAsc(String photographerId, LocalDateTime now);

    List<Event> findByClientId(String clientId);

    List<Event> findByClientIdOrderByDateTimeAsc(String clientId);

    List<Event> findByClientIdAndStatusOrderByDateTimeAsc(String clientId, EventStatus status);

    int countByClientIdAndStatus(String clientId, EventStatus status);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.photographerId = :photographerId "
            + "AND e.status <> com.gomes.photographer_manager.domain.event.EventStatus.CANCELLED "
            + "AND e.dateTime >= :start AND e.dateTime < :end")
    int countThisMonth(@Param("photographerId") String photographerId,
                       @Param("start") LocalDateTime start,
                       @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.photographerId = :photographerId AND e.status = :status")
    int countByPhotographerIdAndStatus(@Param("photographerId") String photographerId,
                                       @Param("status") EventStatus status);

    @Query("SELECT COUNT(DISTINCT e.clientId) FROM Event e WHERE e.photographerId = :photographerId "
            + "AND e.clientId IS NOT NULL AND e.dateTime >= :start AND e.dateTime < :end")
    int countDistinctClientsBetween(@Param("photographerId") String photographerId,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Query("SELECT e.status AS status, COUNT(e) AS total, COALESCE(SUM(e.value), 0) AS totalValue "
            + "FROM Event e WHERE e.photographerId = :photographerId GROUP BY e.status")
    List<EventStatusAggregate> aggregateByStatus(@Param("photographerId") String photographerId);

    interface EventStatusAggregate {
        EventStatus getStatus();

        long getTotal();

        java.math.BigDecimal getTotalValue();
    }
}
