package org.dga.taxiservice.infrastructure.rest.mapper

import org.dga.taxiservice.domain.event.*
import org.dga.taxiservice.domain.model.RideView
import org.dga.taxiservice.infrastructure.rest.dto.RideEventResponse
import org.dga.taxiservice.infrastructure.rest.dto.RideHistoryResponse
import org.dga.taxiservice.infrastructure.rest.dto.RideResponse
import java.util.UUID

fun RideView.toResponse(): RideResponse {
    return RideResponse(
        rideId = this.rideId,
        userId = this.userId,
        driverId = this.driverId,
        origin = this.origin,
        destination = this.destination,
        status = this.status,
        updatedAt = this.updatedAt
    )
}

fun List<RideEvent>.toHistoryResponse(rideId: UUID): RideHistoryResponse {
    return RideHistoryResponse(
        rideId = rideId,
        events = this.map { it.toEventResponse() }
    )
}

fun RideEvent.toEventResponse(): RideEventResponse {
    return when (this) {
        is RideCreatedEvent -> RideEventResponse(
            eventType = "RideCreated",
            occurredAt = this.time,
            details = mapOf(
                "userId" to this.userId,
                "origin" to this.origin,
                "destination" to this.destination
            )
        )

        is RideAcceptedEvent -> RideEventResponse(
            eventType = "RideAccepted",
            occurredAt = this.time,
            details = mapOf(
                "driverId" to this.driverId
            )
        )

        is RideWaitingEvent -> RideEventResponse(
            eventType = "RideWaiting",
            occurredAt = this.time,
            details = emptyMap()
        )

        is RideDrivingEvent -> RideEventResponse(
            eventType = "RideDriving",
            occurredAt = this.time,
            details = emptyMap()
        )

        is RideFinishedEvent -> RideEventResponse(
            eventType = "RideFinished",
            occurredAt = this.time,
            details = emptyMap()
        )

        is RideCanceledEvent -> RideEventResponse(
            eventType = "RideCanceled",
            occurredAt = this.time,
            details = mapOf(
                "canceledBy" to this.canceledBy
            )
        )
    }
}
