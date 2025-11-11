package org.dga.taxiservice.infrastructure.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class CreateRideRequest(
    @field:NotNull
    val userId: UUID,
    @field:NotBlank
    val origin: String,
    @field:NotBlank
    val destination: String,
)
