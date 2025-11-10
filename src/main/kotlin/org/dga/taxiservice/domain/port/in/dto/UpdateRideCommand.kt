package org.dga.taxiservice.domain.port.`in`.dto

import java.util.UUID

data class UpdateRideCommand(
    val rideId: UUID,
    val status: String,
    val driverId: UUID?,
)
