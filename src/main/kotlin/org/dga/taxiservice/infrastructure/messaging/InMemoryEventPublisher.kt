package org.dga.taxiservice.infrastructure.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.dga.taxiservice.domain.event.RideEvent
import org.dga.taxiservice.domain.port.out.EventPublisher
import org.dga.taxiservice.domain.port.out.RideProjector
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryEventPublisher(
    private val rideProjector: RideProjector,
) : EventPublisher {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val semaphore = Semaphore(20)
    private val locks = ConcurrentHashMap<UUID, Mutex>()

    override fun publish(event: RideEvent) {
        val mutex = locks.computeIfAbsent(event.rideId) { Mutex() }

        scope.launch {
            semaphore.withPermit {
                mutex.withLock {
                    rideProjector.project(event)
                }
            }
        }
    }
}
