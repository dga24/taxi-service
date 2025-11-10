package org.dga.taxiservice.domain.event

import java.time.LocalDateTime
import java.util.UUID

sealed interface RideEvent {
    val rideId: UUID
    val time: LocalDateTime
}
