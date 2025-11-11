package org.dga.taxiservice.infrastructure.rest.dto

import org.dga.taxiservice.domain.model.Status
import java.time.Instant
import java.util.UUID

data class RideResponse(
    val rideId: UUID,
    val userId: UUID,
    val driverId: UUID?,
    val origin: String,
    val destination: String,
    val status: Status,
    val updatedAt: Instant,
)
