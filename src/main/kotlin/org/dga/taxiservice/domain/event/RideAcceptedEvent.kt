package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class RideAcceptedEvent(
    override val rideId: UUID,
    val driverId: UUID,
    override val time: LocalDateTime,
) : RideEvent
