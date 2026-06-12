package com.gomes.photographer_manager.domain.photographer;

import com.gomes.photographer_manager.domain.event.response.EventResponse;
import com.gomes.photographer_manager.domain.event.response.EventStatsResponse;
import com.gomes.photographer_manager.domain.sale.response.SaleResponse;
import com.gomes.photographer_manager.domain.sale.response.SaleStatsResponse;

import java.util.List;

public record PhotographerDashboardResponse(
        List<EventResponse> upcomingEvents,
        List<SaleResponse> recentSales,
        EventStatsResponse eventStats,
        SaleStatsResponse saleStats,
        int teamMembersCount
) {
}
