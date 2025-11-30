package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class OutBoxEvent(
    val id: UUID,
    val rideId: UUID,
    val eventType: String,
    val eventPayload: String,
    val createdAt: LocalDateTime,
    val publishedAt: LocalDateTime?,
    val published: Boolean,
)
