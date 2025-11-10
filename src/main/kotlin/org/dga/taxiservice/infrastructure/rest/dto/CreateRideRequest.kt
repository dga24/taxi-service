package org.dga.taxiservice.infrastructure.rest.dto

import java.util.UUID

data class CreateRideRequest (
    val userId: UUID,
    val origin: String,
    val destination: String
)
