package org.dga.taxiservice.infrastructure.rest.dto

import java.util.UUID

data class UpdateRideRequest (
    val status: String,
    val driverId: UUID? = null
)
