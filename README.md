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


