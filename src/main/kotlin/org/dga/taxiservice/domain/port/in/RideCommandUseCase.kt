package org.dga.taxiservice.domain.port.`in`

import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import java.util.UUID

interface RideCommandUseCase {

    fun createRide(command: CreateRideCommand): UUID

    fun updateRide(command: UpdateRideCommand)
}
