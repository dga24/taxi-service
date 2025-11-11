package org.dga.taxiservice.infrastructure.rest.dto

data class RidesPageResponse(
    val rides: List<RideResponse>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
)
