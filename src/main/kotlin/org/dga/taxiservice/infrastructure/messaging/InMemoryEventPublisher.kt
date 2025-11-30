package org.dga.taxiservice.infrastructure.messaging

import org.dga.taxiservice.domain.event.OutBoxEvent
import org.dga.taxiservice.domain.port.out.EventPublisher
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
@ConditionalOnProperty(value = ["sqs.enabled"], havingValue = "false")
class InMemoryEventPublisher(
    private val rideProjector: RideProjector,
) : EventPublisher {

    override fun publish(event: OutBoxEvent) {
        rideProjector.project(event)
    }
}
