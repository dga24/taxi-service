package org.dga.taxiservice.domain.model

import org.dga.taxiservice.domain.event.RideAcceptedEvent
import org.dga.taxiservice.domain.event.RideCanceledEvent
import org.dga.taxiservice.domain.event.RideCreatedEvent
import org.dga.taxiservice.domain.event.RideEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RideAggregateTest {

    @Test
    fun `rehydrate should throw NoSuchElementException when events list is empty`() {
        val emptyEvents = emptyList<RideEvent>()

        assertThrows<NoSuchElementException> {
            RideAggregate.rehydrate(emptyEvents)
        }
    }

    @Test
    fun `rehydrate should successfully reconstruct aggregate from events`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val events = listOf(
            RideCreatedEvent(
                rideId = rideId,
                userId = userId,
                origin = ORIGIN,
                destination = DESTINATION,
                time = LocalDateTime.now()
            )
        )

        val aggregate = RideAggregate.rehydrate(events = events)

        // Then
        assertNotNull(aggregate)
        assertEquals(rideId, aggregate.rideId)
        assertEquals(Status.WAITING, aggregate.status)
    }

    @Test
    fun `create should generate RideCreatedEvent`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val origin = ORIGIN
        val destination = DESTINATION

        // When
        val aggregate = RideAggregate.create(rideId, userId, origin, destination)

        // Then
        assertNotNull(aggregate)
        assertEquals(rideId, aggregate.rideId)
        assertEquals(Status.WAITING, aggregate.status)
        assertEquals(1, aggregate.events.size)
    }

    @Test
    fun `rehydrate should NOT include historical events in uncommitted events list`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val driverId = UUID.randomUUID()

        val pastEvents = listOf(
            RideCreatedEvent(
                rideId = rideId,
                userId = userId,
                origin = ORIGIN,
                destination = DESTINATION,
                time = LocalDateTime.now()
            ),
            RideAcceptedEvent(rideId = rideId, driverId = driverId, time = LocalDateTime.now())
        )

        // When
        val aggregate = RideAggregate.rehydrate(events = pastEvents)

        // Then
        assertEquals(0, aggregate.events.size)
        assertEquals(Status.ACCEPTED, aggregate.status)
    }

    @Test
    fun `changeStatus from new ride should throw exception for invalid status transitions`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val aggregate = RideAggregate.create(
            rideId = rideId,
            userId = userId,
            origin = ORIGIN,
            destination = DESTINATION
        )

        // When / Then
        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.FINISHED)
        }
        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.DRIVING)
        }
    }

    @Test
    fun `changeStatus from accepted ride should throw exception for invalid status transitions`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val driverId = UUID.randomUUID()
        val pastEvents = listOf(
            RideCreatedEvent(rideId, userId, ORIGIN, DESTINATION, LocalDateTime.now()),
            RideAcceptedEvent(rideId, driverId, LocalDateTime.now())
        )

        val aggregate = RideAggregate.rehydrate(pastEvents)

        // When / Then
        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.ACCEPTED)
        }
    }

    @Test
    fun `changeStatus from canceled should throw exception`() {
        // Given
        val rideId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val pastEvents = listOf(
            RideCreatedEvent(rideId, userId, ORIGIN, DESTINATION, LocalDateTime.now()),
            RideCanceledEvent(rideId, USER, LocalDateTime.now())
        )

        val aggregate = RideAggregate.rehydrate(pastEvents)

        // When / Then
        assertEquals(Status.CANCELED, aggregate.status)

        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.FINISHED)
        }
        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.DRIVING)
        }
        assertThrows<IllegalStateException> {
            aggregate.changeStatus(Status.ACCEPTED, UUID.randomUUID())
        }
    }

    @Test
    fun `changeStatus to ACCEPTED should require driverId`() {
        // Given
        val aggregate = RideAggregate.create(UUID.randomUUID(), UUID.randomUUID(), ORIGIN, DESTINATION)

        // When / Then
        assertThrows<IllegalArgumentException> {
            aggregate.changeStatus(Status.ACCEPTED, driverId = null)
        }
    }

    private companion object {
        private const val ORIGIN = "Origin A"
        private const val DESTINATION = "Destination"
        private const val USER = "USER"
    }
}
