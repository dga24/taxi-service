package org.dga.taxiservice.infrastructure.rest.exceptions

data class ErrorResponse(
    val message: String,
    val errors: List<String>? = null,
)

