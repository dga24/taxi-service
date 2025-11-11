package org.dga.taxiservice.infrastructure.rest.dto

import java.time.LocalDateTime
import java.util.UUID

data class RideHistoryResponse(
    val rideId: UUID,
    val events: List<RideEventResponse>,
)

data class RideEventResponse(
    val eventType: String,
    val occurredAt: LocalDateTime,
    val details: Map<String, Any?>,
)
