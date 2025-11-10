package org.dga.taxiservice.infrastructure.perssistence

import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.port.out.EventStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcEventStore (
    @Autowired
    private val jdbcOps: NamedParameterJdbcTemplate,
) : EventStore {

    override fun append(rideId: UUID, newEvents: List<RideEvent>) {

    }

    override fun load(rideId: UUID): List<RideEvent> {
        // Implementation to load events from the database using jdbcOps
        return emptyList()
    }
}
