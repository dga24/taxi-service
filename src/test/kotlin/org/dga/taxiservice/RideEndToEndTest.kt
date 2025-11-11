package org.dga.taxiservice

import org.dga.taxiservice.infrastructure.rest.dto.CreateRideRequest
import org.dga.taxiservice.infrastructure.rest.dto.UpdateRideRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RideEndToEndTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `complete ride lifecycle`() {
        val userId = UUID.randomUUID()
        val driverId = UUID.randomUUID()

        // Create
        val rideId = restTemplate.postForEntity(
            "/api/v1/rides",
            CreateRideRequest(userId, "Origin", "Destination"),
            UUID::class.java
        ).body!!

        // Accept
        restTemplate.exchange(
            "/api/v1/rides/$rideId",
            HttpMethod.PUT,
            HttpEntity(UpdateRideRequest("ACCEPTED", driverId)),
            Void::class.java
        )

        // Drive
        restTemplate.exchange(
            "/api/v1/rides/$rideId",
            HttpMethod.PUT,
            HttpEntity(UpdateRideRequest("DRIVING", null)),
            Void::class.java
        )

        // Finish
        restTemplate.exchange(
            "/api/v1/rides/$rideId",
            HttpMethod.PUT,
            HttpEntity(UpdateRideRequest("FINISHED", null)),
            Void::class.java
        )

        // Query (CQRS)
        val ride = restTemplate.getForEntity("/api/v1/rides/$rideId", String::class.java)
        assertEquals(HttpStatus.OK, ride.statusCode)
        assert(ride.body!!.contains("FINISHED"))

        // History (Event Sourcing)
        val history = restTemplate.getForEntity("/api/v1/rides/$rideId/history", String::class.java)
        assertEquals(HttpStatus.OK, history.statusCode)
        assert(history.body!!.contains("RideCreated"))
        assert(history.body!!.contains("RideFinished"))
    }

    @Test
    fun `query non existent ride returns NOT_FOUND`() {
        val response = restTemplate.getForEntity(
            "/api/v1/rides/${UUID.randomUUID()}",
            String::class.java
        )
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
}
