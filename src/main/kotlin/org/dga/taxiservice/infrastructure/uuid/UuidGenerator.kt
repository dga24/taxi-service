package org.dga.taxiservice.infrastructure.uuid

import org.dga.taxiservice.domain.port.out.IdGenerator
import java.util.UUID

class UuidGenerator : IdGenerator {

    override fun generate(): UUID = UUID.randomUUID()
}
