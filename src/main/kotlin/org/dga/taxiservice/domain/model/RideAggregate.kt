package org.dga.taxiservice.domain.model

import org.dga.taxiservice.domain.event.RideAcceptedEvent
import org.dga.taxiservice.domain.event.RideCanceledEvent
import org.dga.taxiservice.domain.event.RideCreatedEvent
import org.dga.taxiservice.domain.event.RideDrivingEvent
import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.event.RideFinishedEvent
import org.dga.taxiservice.domain.event.RideWaitingEvent
import java.time.LocalDateTime
import java.util.UUID

class RideAggregate private constructor(
    val rideId: UUID,
    var status: Status,
    val events: MutableList<RideEvent> = mutableListOf(),
) {

    companion object {
        fun create(rideId: UUID, userId: UUID?, origin: String?, destination: String?): RideAggregate {
            val ride = RideAggregate(
                rideId = rideId,
                status = Status.WAITING,
            )
            val event = RideCreatedEvent(
                rideId = rideId,
                userId = userId!!,
                origin = origin!!,
                destination = destination!!,
                time = LocalDateTime.now(),
            )
            ride.apply(event)
            ride.addUncommitedEvent(event)
            return ride
        }

        fun rehydrate(events: List<RideEvent>): RideAggregate {
            RideAggregate(events.first().rideId, Status.WAITING).apply {
                events.forEach { event ->
                    apply(event)
                }
                return this
            }
        }
    }

    fun changeStatus(newStatus: Status, driverId: UUID? = null) {
        validateStatusChange(from = status, to = newStatus)

        if (newStatus == Status.ACCEPTED) {
            requireNotNull(driverId) { "Driver ID is required when accepting a ride" }
        }

        val event = when (newStatus) {
            Status.WAITING -> RideWaitingEvent(
                rideId = rideId,
                time = LocalDateTime.now(),
            )

            Status.DRIVING -> RideDrivingEvent(
                rideId = rideId,
                time = LocalDateTime.now(),
            )

            Status.ACCEPTED -> RideAcceptedEvent(
                rideId = rideId,
                time = LocalDateTime.now(),
                driverId = driverId!!,
            )

            Status.FINISHED -> RideFinishedEvent(
                rideId = rideId,
                time = LocalDateTime.now(),
            )

            Status.CANCELED -> RideCanceledEvent(
                rideId = rideId,
                time = LocalDateTime.now(),
                canceledBy = "USER",
            )
        }
        apply(event)
        addUncommitedEvent(event)
    }

    private fun validateStatusChange(from: Status, to: Status) {
        val valid = mapOf(
            Status.WAITING to listOf(Status.ACCEPTED, Status.CANCELED),
            Status.ACCEPTED to listOf(Status.DRIVING, Status.CANCELED),
            Status.DRIVING to listOf(Status.FINISHED, Status.CANCELED),
            Status.FINISHED to emptyList(),
            Status.CANCELED to emptyList(),
        )
        if (to !in valid.getValue(from)) {
            throw IllegalStateException("Invalid status change from $from to $to")
        }
    }

    private fun apply(event: RideEvent) {
        status = when (event) {
            is RideCreatedEvent -> Status.WAITING
            is RideAcceptedEvent -> Status.ACCEPTED
            is RideWaitingEvent -> Status.WAITING
            is RideDrivingEvent -> Status.DRIVING
            is RideFinishedEvent -> Status.FINISHED
            is RideCanceledEvent -> Status.CANCELED
        }
    }

    private fun addUncommitedEvent(event: RideEvent) {
        events.add(event)
    }
}
