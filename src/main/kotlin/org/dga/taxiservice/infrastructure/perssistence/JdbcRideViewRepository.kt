package org.dga.taxiservice.infrastructure.perssistence

import org.dga.taxiservice.domain.model.PageResponse
import org.dga.taxiservice.domain.model.RideView
import org.dga.taxiservice.domain.model.Status
import org.dga.taxiservice.domain.port.out.RideViewRepository
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.ceil

@Repository
class JdbcRideViewRepository(
    private val jdbcOps: NamedParameterJdbcTemplate,
) : RideViewRepository {

    override fun findById(rideId: UUID): RideView? {
        val params = mapOf("ride_id" to rideId.toString())
        val results = jdbcOps.query(FIND_BY_ID, params, RideViewRowMapper())
        return results.firstOrNull()
    }

    override fun find(
        rideId: UUID?,
        status: Status?,
        from: LocalDateTime?,
        to: LocalDateTime?,
        page: Int,
        size: Int,
    ): PageResponse<RideView> {
        val offset = page * size
        val params = mapOf(
            "ride_id" to rideId?.toString(),
            "status" to status?.name,
            "from" to from,
            "to" to to,
            "offset" to offset,
            "limit" to size
        )

        val totalElements = jdbcOps.queryForObject(COUNT, params, Int::class.java) ?: 0
        val results = jdbcOps.query(FIND, params, RideViewRowMapper())
        val totalPages = if (size > 0) ceil(totalElements.toDouble() / size).toInt() else 0

        return PageResponse(
            results = results,
            page = page,
            size = size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    class RideViewRowMapper : RowMapper<RideView> {
        override fun mapRow(rs: ResultSet, rowNum: Int): RideView {
            return RideView(
                rideId = UUID.fromString(rs.getString("ride_id")),
                userId = UUID.fromString(rs.getString("user_id")),
                driverId = rs.getString("driver_id")?.let { UUID.fromString(it) },
                origin = rs.getString("origin"),
                destination = rs.getString("destination"),
                status = Status.valueOf(rs.getString("status")),
                updatedAt = rs.getTimestamp("updated_at").toInstant()
            )
        }
    }

    private companion object {
        const val COUNT = """
            SELECT COUNT(1)
            FROM ride_view
            WHERE (:ride_id IS NULL OR ride_id = :ride_id)
            AND (:status IS NULL OR status = :status)
            AND (:from IS NULL OR updated_at >= :from)
            AND (:to IS NULL OR updated_at <= :to)
        """
        const val FIND = """
            SELECT ride_id, user_id, driver_id, origin, destination, status, updated_at
            FROM ride_view
            WHERE (:ride_id IS NULL OR ride_id = :ride_id)
            AND (:status IS NULL OR status = :status)
            AND (:from IS NULL OR updated_at >= :from)
            AND (:to IS NULL OR updated_at <= :to)
            ORDER BY updated_at DESC
            LIMIT :limit OFFSET :offset;
        """
        const val FIND_BY_ID = """
            SELECT ride_id, user_id, driver_id, origin, destination, status, updated_at
            FROM ride_view
            WHERE ride_id = :ride_id;
        """
    }
}
