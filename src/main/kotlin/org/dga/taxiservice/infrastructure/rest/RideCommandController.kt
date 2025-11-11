package org.dga.taxiservice.infrastructure.rest

import jakarta.validation.Valid
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import org.dga.taxiservice.infrastructure.rest.dto.CreateRideRequest
import org.dga.taxiservice.infrastructure.rest.dto.UpdateRideRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/rides")
class RideCommandController(
    private val rideCommandUseCase: RideCommandUseCase,
) {

    @PostMapping
    fun createRide(
        @Valid @RequestBody createRide: CreateRideRequest,
    ): ResponseEntity<UUID> =
        CreateRideCommand(
            userId = createRide.userId,
            origin = createRide.origin,
            destination = createRide.destination
        ).run {
            ResponseEntity.ok(rideCommandUseCase.createRide(this))
        }

    @PutMapping("/{rideId}")
    fun updateRide(
        @Valid @RequestBody updateRide: UpdateRideRequest,
        @PathVariable rideId: UUID,
    ): ResponseEntity<Unit> {
        UpdateRideCommand(
            rideId = rideId,
            status = updateRide.status,
            driverId = updateRide.driverId,
        ).run {
            rideCommandUseCase.updateRide(this)
        }
        return ResponseEntity.ok().build()
    }
}
