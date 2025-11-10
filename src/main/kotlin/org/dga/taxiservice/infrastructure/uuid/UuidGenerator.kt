package org.dga.taxiservice.infrastructure.uuid

import org.dga.taxiservice.domain.port.out.IdGenerator
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidGenerator : IdGenerator {

    override fun generate(): UUID = UUID.randomUUID()
}
