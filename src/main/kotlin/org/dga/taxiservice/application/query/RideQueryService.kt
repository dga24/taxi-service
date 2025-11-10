package org.dga.taxiservice.application.query

import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.port.`in`.RideQueryUseCase
import org.dga.taxiservice.domain.port.out.EventStore
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class RideQueryService(
    private val eventStore: EventStore,
) : RideQueryUseCase {

    override fun getRides() {

    }

    override fun getRidesHistory(rideId: UUID?, from: LocalDateTime?, to: LocalDateTime?): List<RideEvent> {
        return eventStore.load(rideId = rideId!!)
    }
}
