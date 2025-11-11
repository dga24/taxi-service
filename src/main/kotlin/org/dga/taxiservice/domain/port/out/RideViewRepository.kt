package org.dga.taxiservice.domain.port.out

import org.dga.taxiservice.domain.model.PageResponse
import org.dga.taxiservice.domain.model.RideView
import org.dga.taxiservice.domain.model.Status
import java.time.LocalDateTime
import java.util.UUID

interface RideViewRepository {

    fun findById(rideId: UUID): RideView?

    fun find(
        rideId: UUID? = null,
        status: Status? = null,
        from: LocalDateTime? = null,
        to: LocalDateTime? = null,
        page: Int = 0,
        size: Int = 20,
    ): PageResponse<RideView>
}
