package org.dga.taxiservice.domain.port.`in`.dto

import java.util.UUID

data class CreateRideCommand (
    val userId: UUID,
    val origin: String,
    val destination: String
)
