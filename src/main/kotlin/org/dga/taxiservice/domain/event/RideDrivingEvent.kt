package org.dga.taxiservice.domain.event

import java.time.LocalDateTime

data class RideDrivingEvent (
    override val rideId: java.util.UUID,
    override val time: LocalDateTime,
) : RideEvent
