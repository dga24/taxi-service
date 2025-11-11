package org.dga.taxiservice.infrastructure.rest

import org.dga.taxiservice.application.query.RideQueryService
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.infrastructure.rest.dto.RideHistoryResponse
import org.dga.taxiservice.infrastructure.rest.dto.RideResponse
import org.dga.taxiservice.infrastructure.rest.dto.RidesPageResponse
import org.dga.taxiservice.infrastructure.rest.mapper.toHistoryResponse
import org.dga.taxiservice.infrastructure.rest.mapper.toResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rides")
class RideQueryController(
    private val rideQueryService: RideQueryService,
) {

    @GetMapping("/{rideId}/history")
    fun getRideHistory(
        @PathVariable rideId: String,
        @RequestParam(required = false) from: LocalDateTime?,
        @RequestParam(required = false) to: LocalDateTime?,
    ): ResponseEntity<RideHistoryResponse> {
        val rideUUID = UUID.fromString(rideId)
        val events = rideQueryService.getRideHistory(
            rideId = rideUUID,
            from = from,
            to = to
        )
        return ResponseEntity.ok(events.toHistoryResponse(rideUUID))
    }

    @GetMapping("/{rideId}")
    fun getRide(
        @PathVariable rideId: String,
    ): ResponseEntity<RideResponse> {
        val ride = rideQueryService.getRideById(UUID.fromString(rideId))
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ride.toResponse())
    }

    @GetMapping
    fun getRides(
        @RequestParam(required = false) rideId: String?,
        @RequestParam(required = false) status: Status?,
        @RequestParam(required = false) from: LocalDateTime?,
        @RequestParam(required = false) to: LocalDateTime?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<RidesPageResponse> {
        val pageResponse = rideQueryService.getRides(
            rideId = rideId?.let { UUID.fromString(it) },
            status = status,
            from = from,
            to = to,
            page = page,
            pageSize = pageSize
        )
        return ResponseEntity.ok(
            RidesPageResponse(
                rides = pageResponse.results.map { it.toResponse() },
                page = pageResponse.page,
                pageSize = pageResponse.size,
                total = pageResponse.totalElements,
                totalPages = pageResponse.totalPages
            )
        )
    }
}
