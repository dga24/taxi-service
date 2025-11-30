package org.dga.taxiservice.infrastructure.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import org.dga.taxiservice.application.command.RideCommandService
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.out.EventRepository
import org.dga.taxiservice.domain.port.out.IdGenerator
import org.dga.taxiservice.domain.port.out.OutBoxRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class OutboxTest {

    private lateinit var idGenerator: IdGenerator
    private lateinit var objectMapper: ObjectMapper
    private lateinit var eventRepository: EventRepository
    private lateinit var outBoxRepository: OutBoxRepository

    private lateinit var rideCommandService: RideCommandService


    @BeforeEach
    fun setUp() {
        idGenerator = mockk()
        objectMapper = mockk()
        outBoxRepository = mockk()
        eventRepository = mockk()

        rideCommandService = RideCommandService(
            idGenerator,
            eventRepository,
            outBoxRepository,
            objectMapper,
        )
    }

    @Test
    fun `should throw exception when event publish fails`() {
        // Given
        val rideId = UUID.randomUUID()

        every { idGenerator.generate() } returns rideId
        every { eventRepository.append(any(), any()) } just Runs
        every { objectMapper.writeValueAsString(any()) } returns """{"origin":"origin"}"""

        val command = CreateRideCommand(
            UUID.randomUUID(),
            origin = "origin",
            destination = "destination",
        )

        // When/Then - La excepción debe propagarse
        assertThrows<RuntimeException> {
            rideCommandService.createRide(command)
        }

        // Verify - Los métodos fueron llamados antes del fallo
        verify { eventRepository.append(rideId, any()) }
    }
}
