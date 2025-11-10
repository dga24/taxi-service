package org.dga.taxiservice.domain.port.`in`

import org.dga.taxiservice.domain.model.Status
import java.util.UUID

interface RideCommandUseCase {

    fun createRide(command: CreateRideCommand): UUID

    fun updateRide(rideId: UUID, status: Status, driverId: UUID?)
}
