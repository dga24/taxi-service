package org.dga.taxiservice.domain.port.out

import org.dga.taxiservice.domain.event.OutBoxEvent

interface EventPublisher {

    fun publish(event: OutBoxEvent)
}
