package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class RideFinishedEvent(
    override val rideId: UUID,
    override val time: LocalDateTime,
) : RideEvent
