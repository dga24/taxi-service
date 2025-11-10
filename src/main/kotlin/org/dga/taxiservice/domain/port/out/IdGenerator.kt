package org.dga.taxiservice.domain.port.out

import java.util.UUID

interface IdGenerator {

    fun generate(): UUID
}
