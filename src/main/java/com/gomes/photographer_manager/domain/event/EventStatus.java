package com.gomes.photographer_manager.domain.event;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status de um evento")
public enum EventStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
