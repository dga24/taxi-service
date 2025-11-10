package org.dga.taxiservice.infrastructure.perssistence

import org.dga.taxiservice.domain.event.RideAcceptedEvent
import org.dga.taxiservice.domain.event.RideCanceledEvent
import org.dga.taxiservice.domain.event.RideCreatedEvent
import org.dga.taxiservice.domain.event.RideDrivingEvent
import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.event.RideFinishedEvent
import org.dga.taxiservice.domain.event.RideWaitingEvent
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcRideProjector(
    private val jdbcOps: NamedParameterJdbcTemplate,
) : RideProjector {

    override fun project(event: RideEvent) {
        when (event) {
            is RideCreatedEvent -> createView(event)
            is RideAcceptedEvent -> projectAccepted(event)
            is RideWaitingEvent -> projectWaiting(event)
            is RideDrivingEvent -> projectDriving(event)
            is RideFinishedEvent -> projectFinished(event)
            is RideCanceledEvent -> projectCanceled(event)
        }
    }

    private fun projectWaiting(event: RideWaitingEvent) = updateStatus(event.rideId, Status.WAITING)
    private fun projectDriving(event: RideDrivingEvent) = updateStatus(event.rideId, Status.DRIVING)
    private fun projectFinished(event: RideFinishedEvent) = updateStatus(event.rideId, Status.FINISHED)
    private fun projectCanceled(event: RideCanceledEvent) = updateStatus(event.rideId, Status.CANCELED)


    private fun updateStatus(rideId: UUID, status: Status) {
        jdbcOps.update(
            UPDATE_STATUS,
            mapOf(
                "ride_id" to rideId,
                "status" to status
            )
        )
    }

    private fun createView(event: RideCreatedEvent) {
        jdbcOps.update(
            CREATE_VIEW,
            mapOf(
                "ride_id" to event.rideId,
                "user_id" to event.userId,
                "driver_id" to null,
                "origin" to event.origin,
                "destination" to event.destination,
                "status" to Status.WAITING,
                "updated_at" to event.time
            )
        )
    }

    private fun projectAccepted(event: RideAcceptedEvent) {
        jdbcOps.update(
            UPDATE_VIEW,
            mapOf(
                "driver_id" to event.driverId,
                "status" to Status.ACCEPTED,
                "updated_at" to event.time,
                "ride_id" to event.rideId
            )
        )
    }

    private companion object {
        const val CREATE_VIEW = """
            INSERT INTO RIDE_VIEW (RIDE_ID, USER_ID, DRIVER_ID, ORIGIN, DESTINATION, STATUS, UPDATED_AT) 
            VALUES (:ride_id, :user_id, :driver_id, :origin, :destination, :status, :updated_at)
        """
        const val UPDATE_VIEW = """
            UPDATE RIDE_VIEW 
            SET DRIVER_ID = :driver_id, STATUS = :status, UPDATED_AT = :updated_at
            WHERE RIDE_ID = :ride_id
        """
        const val UPDATE_STATUS = """
            UPDATE RIDE_VIEW 
            SET STATUS = :status, UPDATED_AT = CURRENT_TIMESTAMP
            WHERE RIDE_ID = :ride_id
            """
    }
}
