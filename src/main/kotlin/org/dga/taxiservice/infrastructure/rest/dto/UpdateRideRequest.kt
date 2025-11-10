package org.dga.taxiservice.infrastructure.rest.dto

data class UpdateRideRequest (
    val rideId: String,
    val status: String,
    val driverId: String? = null
)
