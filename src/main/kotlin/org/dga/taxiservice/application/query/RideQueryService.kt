package org.dga.taxiservice.application.query

import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.model.PageResponse
import org.dga.taxiservice.domain.model.RideView
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.`in`.RideQueryUseCase
import org.dga.taxiservice.domain.port.out.EventRepository
import org.dga.taxiservice.domain.port.out.RideViewRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class RideQueryService(
    private val eventRepository: EventRepository,
    private val rideViewRepository: RideViewRepository,
) : RideQueryUseCase {

    override fun getRides(
        rideId: UUID?,
        status: Status?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        page: Int,
        pageSize: Int,
    ): PageResponse<RideView> {
        return rideViewRepository.find(
            rideId = rideId,
            status = status,
            from = from,
            to = to,
            page = page,
            size = pageSize
        )
    }

    override fun getRideById(rideId: UUID): RideView? {
        return rideViewRepository.findById(rideId)
    }

    override fun getRideHistory(
        rideId: UUID,
        from: LocalDateTime?,
        to: LocalDateTime?,
    ): List<RideEvent> {
        val events = eventRepository.load(rideId)

        if (from == null && to == null) {
            return events
        }

        return events.filter { event ->
            val eventTime = event.time
            val matchesFrom = from?.let { eventTime >= it } ?: true
            val matchesTo = to?.let { eventTime <= it } ?: true
            matchesFrom && matchesTo
        }
    }
}
