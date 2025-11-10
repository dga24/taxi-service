package org.dga.taxiservice.application.command

import org.dga.taxiservice.domain.model.RideAggregate
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.`in`.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.out.EventStore
import org.dga.taxiservice.domain.port.out.IdGenerator
import java.util.UUID

class RideCommandService(
    private val idGenerator: IdGenerator,
    private val eventStore: EventStore,
) : RideCommandUseCase {

    override fun createRide(command: CreateRideCommand): UUID {
        val id = idGenerator.generate()
        val aggregate = RideAggregate.create(
            rideId = id,
            userId = command.userId,
            origin = command.origin,
            destination = command.destination,
        )
        eventStore.append(rideId = id, newEvents = aggregate.events)
        return id
    }

    override fun updateRide(rideId: UUID, status: Status, driverId: UUID?) {
        val pastEvents = eventStore.load(rideId = rideId)
        val aggregate = RideAggregate.rehydrate(events = pastEvents)
        aggregate.changeStatus(newStatus = status, driverId = driverId)
        eventStore.append(rideId = rideId, newEvents = aggregate.events)
    }
}
