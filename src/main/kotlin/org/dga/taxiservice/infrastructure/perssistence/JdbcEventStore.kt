package org.dga.taxiservice.infrastructure.perssistence

import com.fasterxml.jackson.databind.ObjectMapper
import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.port.out.EventStore
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

@Repository
class JdbcEventStore(
    private val jdbcOps: NamedParameterJdbcTemplate,
    private val objectMapper: ObjectMapper,
) : EventStore {

    override fun append(rideId: UUID, newEvents: List<RideEvent>) {
        var nextSeq = getNextSequence(rideId)

        newEvents.forEach { event ->
            val params = mapOf(
                "ride_id" to rideId.toString(),
                "type" to event::class.simpleName,
                "payload" to objectMapper.writeValueAsString(event),
                "occurred_at" to event.time,
                "seq" to nextSeq++
            )

            jdbcOps.update(APPEND_RIDE, params)
        }
    }

    private fun getNextSequence(rideId: UUID): Int {
        val params = mapOf(
            "ride_id" to rideId
        )
        return (jdbcOps.queryForObject(
            GET_LAST_SEQ_FROM_RIDE,
            params,
            Int::class.java
        ) ?: 0) + 1
    }

    override fun load(rideId: UUID): List<RideEvent> {
        val params = mapOf("ride_id" to rideId.toString())
        return jdbcOps.query(GET_RIDE_DETAILS, params, RideEventRowMapper(objectMapper))
    }


    class RideEventRowMapper(
        private val objectMapper: ObjectMapper,
    ) : RowMapper<RideEvent> {

        override fun mapRow(rs: ResultSet, rowNum: Int): RideEvent {
            val type = rs.getString("type")
            val payload = rs.getString("payload")

            val eventClass = Class.forName("org.dga.taxiservice.domain.event.$type")
            return objectMapper.readValue(payload, eventClass) as RideEvent
        }
    }

    private companion object {
        const val APPEND_RIDE = """
            INSERT INTO events(ride_id, seq, type, payload, occurred_at) 
            values (:ride_id, :seq,:type,:payload,:occurred_at)
        """
        const val GET_LAST_SEQ_FROM_RIDE = """
            SELECT max(seq) FROM events WHERE ride_id = :ride_id
        """
        const val GET_RIDE_DETAILS = """
            SELECT * FROM events WHERE ride_id = :ride_id
        """
    }
}
