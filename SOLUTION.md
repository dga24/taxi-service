# Taxi Service API

Backend for taxi ride management implemented with Event Sourcing and CQRS from https://app.portfo.me/.

## Technologies

- Kotlin 1.9.25
- Spring Boot 3.5.7
- H2 Database
- Gradle

## Architecture

- **Event Sourcing**: All changes stored as immutable events
- **CQRS**: Separation between write (commands) and read (queries)
- **Hexagonal Architecture**: Domain, Application, Infrastructure
- **Outbox Pattern**: Reliable event publishing with transactional guarantees
- **Multi-Environment**: Different event publishers per environment (dev/test/prod)

## Run the application

```bash
./gradlew bootRun
```

API will be available at `http://localhost:8080`

## Run tests

```bash
./gradlew test
```

## API Endpoints
in postman.json is a postman collection for testing

### Create ride
```
POST /api/v1/rides
{
  "userId": "uuid",
  "origin": "Origin address",
  "destination": "Destination address"
}
```

### Update ride status
```
PUT /api/v1/rides/{rideId}
{
  "status": "ACCEPTED|WAITING|CANCELED|DRIVING|FINISHED",
  "driverId": "uuid"  // required only for ACCEPTED
}
```

### Get ride
```
GET /api/v1/rides/{rideId}
```

### List rides (paginated)
```
GET /api/v1/rides?rideId=uuid&status=PENDING&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59&page=0&size=20
```

## Ride states

1. **PENDING** - Ride created
2. **ACCEPTED** - Driver assigned
3. **WAITING** - Driver waiting for passenger
4. **CANCELED** - Ride canceled
5. **DRIVING** - Ride in progress
6. **FINISHED** - Ride completed

## Database

- **Development**: H2 file-based (`./data/taxi-service-db`)
- **Tests**: H2 in-memory

---

## Event Publishing Architecture

The application uses different event publishing strategies based on the environment, controlled by the `sqs.enabled` property.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              Command Side (Write)                        │
├─────────────────────────────────────────────────────────┤
│ RideCommandService                                       │
│   ├─> Save to Event Store (event_store table)          │
│   └─> Save to Outbox (outbox_events table)             │
└─────────────────────────────────────────────────────────┘
                      │
                      ▼
        ┌──────────────────────────┐
        │    ProjectWorker          │
        │   (Siempre activo)        │
        │                           │
        │  @Scheduled(every 1s)     │
        │  ├─ findUnpublished()     │
        │  └─ publish(event)        │
        └──────────┬────────────────┘
                   │
        ┌──────────┴─────────────┐
        │                        │
        ▼                        ▼
┌──────────────────┐   ┌──────────────────┐
│  sqs.enabled     │   │  sqs.enabled     │
│    = false       │   │    = true        │
│  (dev/test)      │   │    (prod)        │
└────────┬─────────┘   └────────┬─────────┘
         │                      │
         ▼                      ▼
┌──────────────────┐   ┌──────────────────┐
│InMemoryPublisher │   │  SqsPublisher    │
│                  │   │                  │
│ publish()        │   │ publish()        │
│  ↓               │   │  ↓               │
│ RideProjector    │   │ AWS SQS          │
│  (Síncrono)      │   │  ↓               │
└──────────────────┘   │ Consumer         │
                       │  ↓               │
                       │ RideProjector    │
                       │  (Asíncrono)     │
                       └──────────────────┘
         │                      │
         └──────────┬───────────┘
                    ▼
        ┌─────────────────────────┐
        │   Query Side (Read)     │
        │                         │
        │  RIDE_VIEW table        │
        │  (Projection)           │
        └─────────────────────────┘
```

### Event Publishers

#### 1. InMemoryEventPublisher (Dev/Test)

**Activation**: `sqs.enabled=false`

**Characteristics**:
- ✅ **Synchronous**: Events projected immediately within the same transaction
- ✅ **Simple**: No external dependencies
- ✅ **Fast**: Ideal for development and testing
- ✅ **No delays**: Read model updated instantly

**Flow**:
```kotlin
POST /rides
  → RideCommandService.createRide()
  → outBoxRepository.save(event)
  → Transaction commits

ProjectWorker (@Scheduled every 1s):
  → findUnPublishedEvents()
  → inMemoryEventPublisher.publish(event)
  → rideProjector.project(event)   // Synchronous
  → markAsPublished()
  → RIDE_VIEW updated

GET /rides/{id}  // ← Available after ~1 second max
```

**Code**:
```kotlin
@Component
@Primary
@ConditionalOnProperty(value = ["sqs.enabled"], havingValue = "false")
class InMemoryEventPublisher(
    private val rideProjector: RideProjector,
) : EventPublisher {
    override fun publish(event: OutBoxEvent) {
        rideProjector.project(event)
    }
}
```

#### 2. SqsEventPublisher (Production)

**Activation**: `sqs.enabled=true`

**Characteristics**:
- ✅ **Asynchronous**: Events published via AWS SQS
- ✅ **Reliable**: Outbox pattern guarantees delivery
- ✅ **Scalable**: Handles high throughput
- ✅ **Resilient**: Automatic retries on failures

**Flow**:
```kotlin
POST /rides
  → RideCommandService.createRide()
  → outBoxRepository.save(event)
  → Transaction commits

ProjectWorker (@Scheduled every 1s):
  → findUnPublishedEvents()
  → sqsEventPublisher.publish(event)
  → Send to AWS SQS
  → markAsPublished()

SQS Consumer (separate service):
  → Receives from SQS
  → rideProjector.project(event)
  → RIDE_VIEW updated
```

**Code**:
```kotlin
@Component
@ConditionalOnProperty(value = ["sqs.enabled"], havingValue = "true")
class SqsEventPublisher(
    environment: Environment,
    private val objectMapper: ObjectMapper,
) : EventPublisher {
    private var queueUrl: String = environment.getProperty("sqs.projections") ?: ""
    private var sqsClient: SqsClient = SqsClient.builder()
        .endpointOverride(URI.create(queueUrl))
        .build()

    override fun publish(event: OutBoxEvent) {
        sqsClient.sendMessage(
            SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(objectMapper.writeValueAsString(event))
                .build()
        )
    }
}
```

---

## Outbox Pattern Implementation

The application implements the **Transactional Outbox Pattern** to guarantee reliable event publishing.

### Why Outbox Pattern?

**Problem**: Dual-write problem
```
try {
    database.save(ride)           // ✅ Success
    messageQueue.publish(event)   // ❌ Network failure
    // Inconsistency! Event not published
} catch (e: Exception) {
    // What to rollback?
}
```

**Solution**: Outbox Pattern
```
@Transactional
fun createRide() {
    eventStore.save(event)      // ✅ Same transaction
    outbox.save(event)          // ✅ Same transaction
    // Both succeed or both rollback
}
// Later: Worker publishes from outbox
```

### Database Schema

```sql
-- Event Store (Write side)
CREATE TABLE event_store (
    id UUID PRIMARY KEY,
    ride_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_data TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version INTEGER NOT NULL
);

-- Outbox (Transactional publishing queue)
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    ride_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP
);

-- Read Model (Query side)
CREATE TABLE RIDE_VIEW (
    ride_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    driver_id UUID,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### ProjectWorker (Outbox Processor)

Processes unpublished events and publishes them via SQS.

**Key Features**:
- ✅ **Scheduled**: Runs every 1 second (`fixedDelay = 1000`)
- ✅ **Grouped by Ride**: Processes events per ride to maintain order
- ✅ **Fault Isolated**: Failure in one ride doesn't block others
- ✅ **Automatic Retry**: Failed events remain unpublished and retry
- ✅ **Transactional**: Uses `@Transactional` with `FOR UPDATE SKIP LOCKED`

**Code**:
```kotlin
@Component
class ProjectWorker(
    private val outBoxRepository: OutBoxRepository,
    private val eventPublisher: EventPublisher,
) {
    @Scheduled(fixedDelay = 1000)
    @Transactional // Required for FOR UPDATE SKIP LOCKED
    fun processOutbox() {
        val unPublishedEvents = outBoxRepository.findUnPublishedEvents()

        unPublishedEvents.groupBy { it.rideId }.forEach { (rideId, events) ->
            try {
                events.forEach { event ->
                    eventPublisher.publish(event)
                    outBoxRepository.markAsPublished(event.id)
                }
            } catch (e: Exception) {
                logger.error {
                    "Failed to publish events for ride $rideId: ${e.message}. " +
                    "Remaining events will be retried."
                }
            }
        }
    }
}
```

### Event Ordering Guarantees

**Query with Order**:
```sql
SELECT * FROM outbox_events
WHERE published = false
ORDER BY created_at ASC      -- ← Chronological order
LIMIT 100                     -- ← Batch processing
FOR UPDATE SKIP LOCKED        -- ← Multi-instance support
```

**Processing Flow**:
```
Timeline:
─────────────────────────────────────────
10:00:00.100 → RideCreated   → Outbox
10:00:00.200 → RideAccepted  → Outbox
10:00:00.300 → RideDriving   → Outbox
10:00:00.400 → RideFinished  → Outbox
10:00:01.000 → ProjectWorker executes

Retrieved in order:
1. RideCreated   → publish ✅ → mark published
2. RideAccepted  → publish ✅ → mark published
3. RideDriving   → publish ✅ → mark published
4. RideFinished  → publish ✅ → mark published

✅ ORDER GUARANTEED
```

### Multi-Instance Support (Horizontal Scaling)

**Problem**: Multiple instances processing the same events

**Solution**: `FOR UPDATE SKIP LOCKED` (PostgreSQL)

```
┌─────────────────────────────────────────────────┐
│            Database (Outbox)                     │
│  [Event 1-300 unpublished]                      │
└───────┬──────────────────────┬──────────────────┘
        │                      │
   ┌────▼────┐           ┌────▼────┐           ┌────▼────┐
   │Instance │           │Instance │           │Instance │
   │   1     │           │   2     │           │   3     │
   └─────────┘           └─────────┘           └─────────┘

T=0s: All instances execute SELECT ... FOR UPDATE SKIP LOCKED

Instance 1: Locks events   1-100 → Processes   1-100 ✅
Instance 2: Locks events 101-200 → Processes 101-200 ✅
Instance 3: Locks events 201-300 → Processes 201-300 ✅

✅ ZERO collisions, maximum throughput!
```

**Query**:
```kotlin
const val FIND_UNPUBLISHED = """
    SELECT * FROM outbox_events
    WHERE published = false
    ORDER BY created_at ASC
    LIMIT 100
    FOR UPDATE SKIP LOCKED  -- Each instance locks different rows
"""
```

---

## Environment Configuration

Configuration is managed via environment variables and `.env` files.

### Configuration Files

- **`.env.dev`** - Development environment
- **`.env.prod`** - Production environment
- **`application.yaml`** - Main configuration with variable interpolation
- **`application-test.yaml`** - Test-specific overrides

### Environment Variables

| Variable | Dev | Test | Prod | Description |
|----------|-----|------|------|-------------|
| `SPRING_PROFILE` | `dev` | `test` | `prod` | Active Spring profile |
| `SQS_ENABLED` | `false` | `false` | `true` | Event publisher mode |
| `DATABASE_URL` | H2 file | H2 memory | PostgreSQL | Database connection |
| `SQS_PROJECTIONS_ENDPOINT` | - | - | AWS SQS URL | SQS queue for events |

### Example: `.env.dev`

```bash
# Development Environment
SPRING_PROFILE=dev

# Database - H2 file-based
DATABASE_URL=jdbc:h2:file:./data/taxi-service-db
DATABASE_DRIVER=org.h2.Driver
DATABASE_USERNAME=u
DATABASE_PASSWORD=u

# Event Publishing - InMemory (synchronous)
SQS_ENABLED=false
SQS_PROJECTIONS_ENDPOINT=

# Logging
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_APP=DEBUG
```

### Example: `.env.prod`

```bash
# Production Environment
SPRING_PROFILE=prod

# Database - PostgreSQL
DATABASE_URL=jdbc:postgresql://db-host:5432/taxi_service
DATABASE_DRIVER=org.postgresql.Driver
DATABASE_USERNAME=taxi_user
DATABASE_PASSWORD=${DB_PASSWORD}

# Event Publishing - SQS (asynchronous)
SQS_ENABLED=true
SQS_PROJECTIONS_ENDPOINT=https://sqs.us-east-1.amazonaws.com/account/queue

# Logging
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
```

### Running with Environment

```bash
# Development
export $(cat .env.dev | xargs)
./gradlew bootRun

# Production
export $(cat .env.prod | xargs)
./gradlew bootRun

# Or with Docker
docker run --env-file .env.prod taxi-service:latest
```

---

## Testing Strategy

### Unit Tests
- Mock all dependencies
- Test business logic in isolation
- Fast execution

### Integration Tests
- Use `@SpringBootTest` with `@ActiveProfiles("test")`
- H2 in-memory database
- InMemoryEventPublisher (synchronous projection)
- Test transactional behavior

### End-to-End Tests
- Full HTTP request/response cycle
- Verify CQRS consistency
- Test complete ride lifecycle

Example:
```kotlin
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class RideEndToEndTest {
    @Test
    fun `complete ride lifecycle`() {
        // Create ride
        val rideId = restTemplate.postForEntity("/api/v1/rides", request, UUID::class.java).body

        // Update status
        restTemplate.put("/api/v1/rides/$rideId", UpdateRideRequest("ACCEPTED", driverId))

        // Query ride (CQRS - projection already updated)
        val ride = restTemplate.getForEntity("/api/v1/rides/$rideId", RideResponse::class.java)
        assertEquals(HttpStatus.OK, ride.statusCode)
        assertEquals("ACCEPTED", ride.body?.status)
    }
}
```

---

## Performance Characteristics

### Development (InMemory)
- **Latency**: < 10ms (synchronous projection)
- **Throughput**: Limited by single transaction
- **Consistency**: Immediate (read-your-writes)

### Production (SQS + Outbox)
- **Latency**: ~1-2s (eventual consistency)
- **Throughput**: High (async processing)
- **Scalability**: Horizontal (multiple workers)
- **Reliability**: Guaranteed delivery (outbox pattern)

---

## Monitoring and Observability

### Key Metrics to Monitor

1. **Outbox Queue Size**
   ```sql
   SELECT COUNT(*) FROM outbox_events WHERE published = false
   ```
   Alert if > 1000 (backlog)

2. **Publishing Lag**
   ```sql
   SELECT MAX(created_at) FROM outbox_events WHERE published = false
   ```
   Alert if > 5 minutes

3. **Failed Events**
   - Check application logs for "Failed to publish events"
   - Investigate ride-specific failures

4. **Projection Lag** (Production)
   - Compare event store timestamps with RIDE_VIEW timestamps
   - Monitor SQS queue depth

### Logs

```kotlin
// Success
logger.debug { "Published event ${event.eventType} for ride $rideId" }

// Failure
logger.error {
    "Failed to publish events for ride $rideId: ${e.message}. " +
    "Remaining events will be retried."
}
```


