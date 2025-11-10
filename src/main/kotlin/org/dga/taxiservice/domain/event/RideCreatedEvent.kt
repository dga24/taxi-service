package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class RideCreatedEvent(
    override val rideId: UUID,
    val userId: UUID,
    val origin: String,
    val destination: String,
    override val time: LocalDateTime,
) : RideEvent
