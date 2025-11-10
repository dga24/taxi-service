INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           1,
           'RideCreatedEvent',
           '{"rideId":"11111111-1111-1111-1111-111111111111","userId":"user-123","origin":"Gran Via 45","destination":"Plaza Catalunya","time":"2025-11-10T10:00:00"}',
           '2025-11-10T10:00:00'
       );

INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           2,
           'RideAcceptedEvent',
           '{"rideId":"11111111-1111-1111-1111-111111111111","driverId":"driver-777","time":"2025-11-10T10:05:00"}',
           '2025-11-10T10:05:00'
       );

INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           3,
           'RideDrivingEvent',
           '{"rideId":"11111111-1111-1111-1111-111111111111","time":"2025-11-10T10:10:00"}',
           '2025-11-10T10:10:00'
       );

INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           4,
           'RideFinishedEvent',
           '{"rideId":"11111111-1111-1111-1111-111111111111","time":"2025-11-10T10:25:00"}',
           '2025-11-10T10:25:00'
       );


INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           1,
           'RideCreatedEvent',
           '{"rideId":"22222222-2222-2222-2222-222222222222","userId":"user-999","origin":"Diagonal 640","destination":"Sants Estació","time":"2025-11-10T09:00:00"}',
           '2025-11-10T09:00:00'
       );

INSERT INTO events (ride_id, seq, type, payload, occurred_at)
VALUES (
           '22222222-2222-2222-2222-222222222222',
           2,
           'RideCanceledEvent',
           '{"rideId":"22222222-2222-2222-2222-222222222222","canceledBy":"USER","time":"2025-11-10T09:02:00"}',
           '2025-11-10T09:02:00'
       );
