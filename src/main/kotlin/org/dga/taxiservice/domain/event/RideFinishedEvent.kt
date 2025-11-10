package org.dga.taxiservice.domain.event

data class RideFinishedEvent(
    override val rideId: java.util.UUID,
    override val time: java.time.LocalDateTime,
) : RideEvent
