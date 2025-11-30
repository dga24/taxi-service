package org.dga.taxiservice.infrastructure.outbox

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.dga.taxiservice.application.command.RideCommandService
import org.dga.taxiservice.domain.port.`in`.dto.CreateRideCommand
import org.dga.taxiservice.domain.port.out.EventRepository
import org.dga.taxiservice.domain.port.out.OutBoxRepository
import org.dga.taxiservice.domain.port.out.RideViewRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.lang.Thread.sleep
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class OutboxIntegrationTest {

    @SpykBean
    lateinit var outBoxRepository: OutBoxRepository

    @Autowired
    lateinit var eventRepository: EventRepository

    @Autowired
    lateinit var rideCommandService: RideCommandService

    @Autowired
    lateinit var rideViewRepository: RideViewRepository

    @Test
    fun `should create event`() {
        // Given
        val rideId = UUID.randomUUID()
        val cmd = CreateRideCommand(rideId, "o", "d")

        // When/Then
        val id = rideCommandService.createRide(cmd)

        // Verify rollback
        assertTrue(eventRepository.load(id).count() == 1)

        sleep(1100)

        assertEquals(rideViewRepository.findById(id)?.destination, cmd.destination)
    }

    @Test
    fun `should rollback append event if outBox save fails`() {
        // Given
        val rideId = UUID.randomUUID()
        val cmd = CreateRideCommand(rideId, "o", "d")

        every { outBoxRepository.save(any()) } throws RuntimeException()

        assertThrows<RuntimeException> {
            rideCommandService.createRide(cmd)
        }

        // Verify rollback
        assertTrue(eventRepository.load(rideId).isEmpty())
    }
}


