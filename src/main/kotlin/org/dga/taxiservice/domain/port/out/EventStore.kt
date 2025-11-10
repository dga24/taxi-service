package org.dga.taxiservice.domain.port.out

import org.dga.taxiservice.domain.event.RideEvent
import java.util.UUID

interface EventStore {

    fun append(rideId: UUID, newEvents: List<RideEvent>)
    fun load(rideId: UUID): List<RideEvent>
}
