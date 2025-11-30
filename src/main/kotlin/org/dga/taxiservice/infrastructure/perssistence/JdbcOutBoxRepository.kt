package org.dga.taxiservice.infrastructure.perssistence

import org.dga.taxiservice.domain.event.OutBoxEvent
import org.dga.taxiservice.domain.port.out.OutBoxRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
class JdbcOutBoxRepository(
    private val jdbcOperations: NamedParameterJdbcTemplate,
) : OutBoxRepository {
    override fun save(event: OutBoxEvent) {
        val params = mapOf(
            "id" to event.id,
            "ride_id" to event.rideId,
            "event_type" to event.eventType,
            "event_payload" to event.eventPayload,
            "created_at" to event.createdAt,
            "published_at" to event.publishedAt,
            "published" to event.published
        )
        jdbcOperations.update(SAVE, params)
    }

    override fun findUnPublishedEvents(): List<OutBoxEvent> {
        return jdbcOperations.query(FIND_UNPUBLISHED, ROW_MAPPER)
    }

    override fun markAsPublished(id: UUID) {
        val params = mapOf(
            "id" to id,
            "published_at" to LocalDateTime.now()
        )
        jdbcOperations.update(
            MARK_AS_PUBLISHED, params
        )
    }

    private companion object {

        const val SAVE = "INSERT INTO " +
                "outbox_events(id,ride_id,event_type,event_payload,created_at,published_at,published) " +
                "values(:id, :ride_id, :event_type, :event_payload, :created_at,:published_at, :published)"

        const val FIND_UNPUBLISHED = """
            SELECT * FROM outbox_events
            WHERE published = false
            ORDER BY created_at ASC
            LIMIT 100
            FOR UPDATE SKIP LOCKED
        """

        const val MARK_AS_PUBLISHED = "UPDATE outbox_events " +
                "SET published = true, published_at = NOW() " +
                "WHERE id = :id"

        val ROW_MAPPER =
            RowMapper { rs, _ ->
                OutBoxEvent(
                    id = UUID.fromString(rs.getString("id")),
                    rideId = rs.getString("ride_id").let { UUID.fromString(it) },
                    eventType = rs.getString("event_type"),
                    eventPayload = rs.getString("event_payload"),
                    createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                    publishedAt = rs.getTimestamp("published_at")?.toLocalDateTime(),
                    published = rs.getBoolean("published"),

                    )
            }
    }
}
