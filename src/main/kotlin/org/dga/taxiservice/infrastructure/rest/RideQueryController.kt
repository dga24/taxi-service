package org.dga.taxiservice.infrastructure.rest

import org.dga.taxiservice.application.query.RideQueryService
import org.springframework.web.bind.annotation.GetMapping
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

    @GetMapping
    fun getRides(
        @RequestParam(required = false) rideId: String,
        @RequestParam(required = false) from: LocalDateTime,
        @RequestParam(required = false) to: LocalDateTime,
    ) {
        val rideUuid = rideId.let { UUID.fromString(it) }
        rideQueryService.getRidesHistory(rideId = rideUuid, from = from, to = to)
    }
}
