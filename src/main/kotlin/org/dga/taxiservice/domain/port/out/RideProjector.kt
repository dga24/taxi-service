package org.dga.taxiservice.domain.port.out

import org.dga.taxiservice.domain.event.RideEvent

interface RideProjector {

    fun project(event: RideEvent)
}
