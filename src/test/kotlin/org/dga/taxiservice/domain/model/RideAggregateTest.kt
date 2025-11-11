package org.dga.taxiservice.domain.model

import org.dga.taxiservice.domain.event.RideAcceptedEvent
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
                origin = "Origin A",
                destination = "Destination B",
                time = LocalDateTime.now()
            )
        )

        val aggregate = RideAggregate.rehydrate(events)

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
        val origin = "Origin A"
        val destination = "Destination B"

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
            RideCreatedEvent(rideId, userId, "A", "B", LocalDateTime.now()),
            RideAcceptedEvent(rideId, driverId, LocalDateTime.now())
        )

        // When
        val aggregate = RideAggregate.rehydrate(pastEvents)

        // Then
        assertEquals(0, aggregate.events.size)
        assertEquals(Status.ACCEPTED, aggregate.status)
    }
}
