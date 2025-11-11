package org.dga.taxiservice.domain.model

import java.time.Instant
import java.util.UUID

data class RideView(
    val rideId: UUID,
    val userId: UUID,
    val driverId: UUID?,
    val origin: String,
    val destination: String,
    val status: Status,
    val updatedAt: Instant,
)
