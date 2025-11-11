package org.dga.taxiservice.domain.model

data class PageResponse<T>(
    val results: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
)
