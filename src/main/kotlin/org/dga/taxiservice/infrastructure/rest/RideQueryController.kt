package org.dga.taxiservice.infrastructure.rest

import org.springframework.data.domain.Limit
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID

@RestController
class RideQueryController {

    @GetMapping
    fun getRides(
        @RequestParam(required = false) rideId: UUID,
        @RequestParam(required = false) from: LocalDateTime,
        @RequestParam(required = false) to: LocalDateTime,
        @RequestParam(required = false) page: Int,
        @RequestParam(required = false) limit: Int
        ){

    }
}
