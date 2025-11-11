package org.dga.taxiservice.infrastructure.rest.dto

import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UpdateRideRequest(
    @field:NotBlank
    val status: String,
    val driverId: UUID? = null,
)
