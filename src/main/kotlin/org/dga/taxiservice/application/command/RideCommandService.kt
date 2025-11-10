package org.dga.taxiservice.application.command

import org.dga.taxiservice.domain.model.RideAggregate
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import org.dga.taxiservice.domain.port.out.EventStore
import org.dga.taxiservice.domain.port.out.IdGenerator
import org.springframework.stereotype.Service
import java.util.UUID

@Service
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

    override fun updateRide(command: UpdateRideCommand) {
        command.run {
            val pastEvents = eventStore.load(rideId = rideId)
            val rideAggregate = RideAggregate.rehydrate(events = pastEvents)
            val s =Status.valueOf(status)
            rideAggregate.changeStatus(newStatus = s, driverId = driverId)
            eventStore.append(rideId = rideId, newEvents = rideAggregate.events)
        }
    }
}
