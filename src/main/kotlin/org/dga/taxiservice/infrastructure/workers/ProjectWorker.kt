package org.dga.taxiservice.infrastructure.workers

import io.github.oshai.kotlinlogging.KotlinLogging
import org.dga.taxiservice.domain.port.out.EventPublisher
import org.dga.taxiservice.domain.port.out.OutBoxRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class ProjectWorker(
    private val outBoxRepository: OutBoxRepository,
    private val eventPublisher: EventPublisher,
) {

    @Scheduled(fixedDelay = 1000)
    @Transactional // Necesario para FOR UPDATE SKIP LOCKED
    fun processOutbox() {
        val unPublishedEvents = outBoxRepository.findUnPublishedEvents()
        unPublishedEvents.groupBy { it.rideId }.forEach { (rideId, events) ->
            try {
                events.forEach { event ->
                    eventPublisher.publish(event)
                    outBoxRepository.markAsPublished(event.id)
                }
            } catch (e: Exception) {
                logger.error {
                    "Failed to publish events for ride $rideId: ${e.message}. " +
                            "Remaining events will be retried."
                }
            }
        }
    }
}


