package com.gomes.photographer_manager.domain.photographer;

import com.gomes.photographer_manager.domain.event.EventService;
import com.gomes.photographer_manager.domain.event.response.EventResponse;
import com.gomes.photographer_manager.domain.event.response.EventStatsResponse;
import com.gomes.photographer_manager.domain.photographer.response.PublicPhotographerDTO;
import com.gomes.photographer_manager.domain.photographer.team.TeamMemberRepository;
import com.gomes.photographer_manager.domain.sale.SaleService;
import com.gomes.photographer_manager.domain.sale.response.SaleResponse;
import com.gomes.photographer_manager.domain.sale.response.SaleStatsResponse;
import com.gomes.photographer_manager.domain.usuario.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/photographer")
@Tag(name = "Painel do Fotógrafo", description = "Dados agregados do painel do fotógrafo")
public class PhotographerController {

    private final EventService eventService;
    private final SaleService saleService;
    private final TeamMemberRepository teamMemberRepository;
    private final PhotographerService photographerService;

    public PhotographerController(EventService eventService,
                                  SaleService saleService,
                                  TeamMemberRepository teamMemberRepository,
                                  PhotographerService photographerService) {
        this.eventService = eventService;
        this.saleService = saleService;
        this.teamMemberRepository = teamMemberRepository;
        this.photographerService = photographerService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Painel do fotógrafo", description = "Retorna eventos futuros, vendas recentes e estatísticas do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Painel retornado com sucesso")
    public ResponseEntity<PhotographerDashboardResponse> dashboard(@AuthenticationPrincipal User user) {
        String photographerId = user.getId();

        List<EventResponse> upcomingEvents = eventService.findUpcoming(photographerId);
        List<SaleResponse> recentSales = saleService.findRecent(photographerId);
        EventStatsResponse eventStats = eventService.getStats(photographerId);
        SaleStatsResponse saleStats = saleService.getStats(photographerId);
        int teamMembersCount = (int) teamMemberRepository.countByPhotographerId(photographerId);

        PhotographerDashboardResponse response = new PhotographerDashboardResponse(
                upcomingEvents,
                recentSales,
                eventStats,
                saleStats,
                teamMembersCount
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Estatísticas do painel", description = "Retorna métricas resumidas do mês atual do fotógrafo autenticado")
    @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso")
    public ResponseEntity<DashboardStatsResponse> dashboardStats(@AuthenticationPrincipal User user) {
        String photographerId = user.getId();
        LocalDate now = LocalDate.now();

        int eventsThisMonth = eventService.countThisMonth(photographerId, now);
        BigDecimal revenueThisMonth = saleService.revenueThisMonth(photographerId, now);
        int pendingDeliveries = eventService.countByStatus(photographerId, "SCHEDULED");
        int newClients = eventService.countNewClientsThisMonth(photographerId, now);

        return ResponseEntity.ok(new DashboardStatsResponse(
                eventsThisMonth, revenueThisMonth, pendingDeliveries, newClients));
    }
}
