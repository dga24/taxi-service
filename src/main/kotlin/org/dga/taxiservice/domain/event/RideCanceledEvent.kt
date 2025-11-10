package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class RideCanceledEvent(
    override val rideId: UUID,
    val canceledBy: String, // "USER" or "DRIVER"
    override val time: LocalDateTime
) : RideEvent
