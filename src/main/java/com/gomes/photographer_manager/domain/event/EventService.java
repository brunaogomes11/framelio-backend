package com.gomes.photographer_manager.domain.event;

import com.gomes.photographer_manager.domain.event.request.EventRequest;
import com.gomes.photographer_manager.domain.event.response.EventResponse;
import com.gomes.photographer_manager.domain.event.response.EventStatsResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository repository;

    public EventService(EventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EventResponse create(EventRequest request, String photographerId) {
        Event event = new Event(request, photographerId);
        return new EventResponse(repository.save(event));
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findAll(String photographerId, EventStatus status, LocalDate date) {
        List<Event> events = status != null
                ? repository.findByPhotographerIdAndStatusOrderByDateTimeAsc(photographerId, status)
                : repository.findByPhotographerIdOrderByDateTimeAsc(photographerId);

        return events.stream()
                .filter(event -> date == null || event.getDateTime().toLocalDate().equals(date))
                .map(EventResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse findById(String id) {
        return new EventResponse(findEntity(id));
    }

    @Transactional
    public EventResponse update(String id, EventRequest request, String photographerId) {
        Event event = findEntity(id);
        validateOwnership(event, photographerId);
        event.update(request);
        return new EventResponse(repository.save(event));
    }

    @Transactional
    public void delete(String id, String photographerId) {
        Event event = findEntity(id);
        validateOwnership(event, photographerId);
        repository.delete(event);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findUpcoming(String photographerId) {
        return repository
                .findTop5ByPhotographerIdAndDateTimeAfterOrderByDateTimeAsc(photographerId, LocalDateTime.now())
                .stream()
                .map(EventResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventStatsResponse getStats(String photographerId) {
        List<EventRepository.EventStatusAggregate> aggregates = repository.aggregateByStatus(photographerId);

        int totalEvents = 0;
        int scheduledCount = 0;
        int completedCount = 0;
        int cancelledCount = 0;
        BigDecimal expectedRevenue = BigDecimal.ZERO;

        for (EventRepository.EventStatusAggregate aggregate : aggregates) {
            int count = (int) aggregate.getTotal();
            totalEvents += count;

            switch (aggregate.getStatus()) {
                case SCHEDULED -> {
                    scheduledCount = count;
                    expectedRevenue = expectedRevenue.add(safeValue(aggregate.getTotalValue()));
                }
                case IN_PROGRESS -> expectedRevenue = expectedRevenue.add(safeValue(aggregate.getTotalValue()));
                case COMPLETED -> completedCount = count;
                case CANCELLED -> cancelledCount = count;
            }
        }

        return new EventStatsResponse(totalEvents, scheduledCount, completedCount, cancelledCount, expectedRevenue);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findByClient(String clientId, EventStatus status) {
        List<Event> events = status != null
                ? repository.findByClientIdAndStatusOrderByDateTimeAsc(clientId, status)
                : repository.findByClientIdOrderByDateTimeAsc(clientId);

        return events.stream()
                .map(EventResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public int countThisMonth(String photographerId, LocalDate month) {
        LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return repository.countThisMonth(photographerId, start, end);
    }

    @Transactional(readOnly = true)
    public int countByStatus(String photographerId, String status) {
        return repository.countByPhotographerIdAndStatus(photographerId, EventStatus.valueOf(status));
    }

    @Transactional(readOnly = true)
    public int countNewClientsThisMonth(String photographerId, LocalDate month) {
        LocalDateTime start = month.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1);
        return repository.countDistinctClientsBetween(photographerId, start, end);
    }

    private Event findEntity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
    }

    private void validateOwnership(Event event, String photographerId) {
        if (!event.getPhotographerId().equals(photographerId)) {
            throw new DataIntegrityViolationException("O evento não pertence ao fotógrafo autenticado");
        }
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
