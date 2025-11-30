package org.dga.taxiservice.domain.port.out

import org.dga.taxiservice.domain.event.OutBoxEvent
import java.util.UUID

interface OutBoxRepository {

    fun save(event: OutBoxEvent)

    fun findUnPublishedEvents(): List<OutBoxEvent>

    fun markAsPublished(id: UUID)
}
