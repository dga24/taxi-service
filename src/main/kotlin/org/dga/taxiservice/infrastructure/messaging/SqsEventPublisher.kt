package org.dga.taxiservice.infrastructure.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dga.taxiservice.domain.event.OutBoxEvent
import org.dga.taxiservice.domain.port.out.EventPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.net.URI

@Component
@ConditionalOnProperty(value = ["sqs.enabled"], havingValue = "true")
class SqsEventPublisher(
    environment: Environment,
    private val objectMapper: ObjectMapper,
) : EventPublisher {

    private var queueUrl: String = environment.getProperty(SQS_ENDPOINT) ?: ""
    private var sqsClient: SqsClient =
        SqsClient.builder()
            .endpointOverride(URI.create(this.queueUrl))
            .build()


    override fun publish(event: OutBoxEvent) {
        sqsClient.sendMessage(
            SendMessageRequest
                .builder()
                .queueUrl(queueUrl)
                .messageBody(objectMapper.writeValueAsString(event))
                .build()
        )
    }

    private companion object {
        const val SQS_ENDPOINT = "sqs.projections"
    }
}
