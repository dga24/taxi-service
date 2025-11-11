package org.dga.taxiservice.application.command

import org.dga.taxiservice.domain.model.RideAggregate
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import org.dga.taxiservice.domain.port.out.EventRepository
import org.dga.taxiservice.domain.port.out.IdGenerator
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

//In-memory EventPublisher is synchronous
// in production would use separate transactions with message queue
@Service
@Transactional
class RideCommandService(
    private val idGenerator: IdGenerator,
    private val eventRepository: EventRepository,
    private val rideProjector: RideProjector,
) : RideCommandUseCase {

    override fun createRide(command: CreateRideCommand): UUID {
        val id = idGenerator.generate()
        val aggregate = RideAggregate.create(
            rideId = id,
            userId = command.userId,
            origin = command.origin,
            destination = command.destination,
        )
        eventRepository.append(rideId = id, newEvents = aggregate.events)
        rideProjector.project(event = aggregate.events.last())
        return id
    }

    override fun updateRide(command: UpdateRideCommand) {
        command.run {
            val pastEvents = eventRepository.load(rideId = rideId)
            if (pastEvents.isEmpty()) {
                throw NoSuchElementException("Ride ${command.rideId} not found")
            }
            val rideAggregate = RideAggregate.rehydrate(events = pastEvents)
            val s = Status.valueOf(status)
            rideAggregate.changeStatus(newStatus = s, driverId = driverId)
            eventRepository.append(rideId = rideId, newEvents = rideAggregate.events)
            rideProjector.project(event = rideAggregate.events.last())
        }
    }
}
