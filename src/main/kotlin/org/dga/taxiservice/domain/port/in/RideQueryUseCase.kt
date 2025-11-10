package org.dga.taxiservice.domain.port.`in`

import org.dga.taxiservice.domain.event.RideEvent
import java.time.LocalDateTime
import java.util.UUID

interface RideQueryUseCase {

    fun getRides()

    fun getRidesHistory(rideId: UUID?, from: LocalDateTime?, to: LocalDateTime?): List<RideEvent>
}
