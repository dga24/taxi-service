package org.dga.taxiservice.application.command

import com.fasterxml.jackson.databind.ObjectMapper
import org.dga.taxiservice.domain.event.OutBoxEvent
import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.model.RideAggregate
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.`in`.RideCommandUseCase
import org.dga.taxiservice.domain.port.`in`.dto.UpdateRideCommand
import org.dga.taxiservice.domain.port.out.EventRepository
import org.dga.taxiservice.domain.port.out.IdGenerator
import org.dga.taxiservice.domain.port.out.OutBoxRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class RideCommandService(
    private val idGenerator: IdGenerator,
    private val eventRepository: EventRepository,
    private val outBoxRepository: OutBoxRepository,
    private val objectMapper: ObjectMapper,
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
        val outBoxEvent = generateOutBoxEvent(event = aggregate.events.last())
        outBoxRepository.save(event = outBoxEvent)
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
            val outBoxEvent = generateOutBoxEvent(event = rideAggregate.events.last())
            outBoxRepository.save(event = outBoxEvent)
        }
    }

    private fun generateOutBoxEvent(event: RideEvent): OutBoxEvent = event.run {
        OutBoxEvent(
            id = idGenerator.generate(),
            rideId = rideId,
            eventType = this::class.simpleName!!,
            eventPayload = objectMapper.writeValueAsString(this),
            createdAt = LocalDateTime.now(),
            published = false,
            publishedAt = null
        )
    }
}
