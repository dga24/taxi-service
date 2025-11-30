Build a Taxi Service Operations backend system that manages ride lifecycles using the Event Sourcing and CQRS patterns. Showcase your expertise in scalable architecture, clean code, and robust design principles.

Objectives
Create a backend system that allows:

Ride Creation

Users can create a new ride with the initial status: PENDING.

Status Updates

Update ride statuses to the following:

ACCEPTED: Driver is on the way to the origin location.

WAITING: Driver has arrived at the origin location and is waiting for the passenger.

CANCELED: Either the driver or passenger canceled the ride.

DRIVING: Driver picked up the passenger and is en route to the destination.

FINISHED: The ride is completed.

Ride History

Fetch a paginated list of rides with their current statuses.

Requirements
Core Architecture
Implement Event Sourcing to store all changes to a ride as a sequence of events.

Use CQRS (Command Query Responsibility Segregation) to separate write operations (commands) and read operations (queries).

Data Store
Use an event store (e.g., PostgreSQL, MongoDB, or an event-centric DB) to persist events. Please use embedded database for the task.

Implement a read model (materialized view) for efficient querying of ride data.

Technical Requirements
Code:

Clean, modular, and extensible code with well-defined boundaries.

Implement proper error handling and validation.

Testing:

Unit tests for at least critical business logic.

Integration tests for the event store and materialized views.

API:

Expose a RESTful or GraphQL API to create rides, update statuses, and fetch rides.

The bonus point
Implement an endpoint that accepts a ride id, a date range and returns historical events for a ride for that period.
