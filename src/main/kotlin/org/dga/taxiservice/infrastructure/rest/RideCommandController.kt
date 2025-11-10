package org.dga.taxiservice.infrastructure.rest

import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import org.dga.taxiservice.infrastructure.rest.dto.CreateRideRequest
import org.dga.taxiservice.infrastructure.rest.dto.UpdateRideRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class RideCommandController(
    private val rideCommandUseCase: RideCommandUseCase,
) {

    @PostMapping
    fun createRide(@RequestBody createRide: CreateRideRequest): ResponseEntity<UUID> =
        CreateRideCommand(
            userId = createRide.userId,
            origin = createRide.origin,
            destination = createRide.destination
        ).run {
            ResponseEntity.ok(rideCommandUseCase.createRide(this))
        }

    @PostMapping
    fun updateRide(@RequestBody updateRide: UpdateRideRequest): ResponseEntity<Unit> {
        UpdateRideCommand(
            rideId = updateRide.rideId,
            status = updateRide.status,
            driverId = updateRide.driverId,
        ).run {
            rideCommandUseCase.updateRide(this)
        }
        return ResponseEntity.ok().build()
    }
}
