package org.dga.taxiservice.infrastructure.messaging

import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.port.out.EventPublisher
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.stereotype.Component

@Component
class InMemoryEventPublisher(
    private val rideProjector: RideProjector,
) : EventPublisher {

    override fun publish(event: RideEvent) {
        rideProjector.project(event)
    }
}
