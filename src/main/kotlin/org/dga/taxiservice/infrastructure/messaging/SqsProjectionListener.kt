package org.dga.taxiservice.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.dga.taxiservice.domain.event.OutBoxEvent
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["sqs.enabled"], havingValue = "true")
class SqsProjectionListener(
    private val rideProjector: RideProjector,
    private val objectMapper: ObjectMapper,
) {

    @SqsListener(value = ["\${sqs.projections}"])
    fun listener(message: String) {
        val outBoxEvent = objectMapper.readValue(message, OutBoxEvent::class.java)
        rideProjector.project(outBoxEvent)
    }
}
