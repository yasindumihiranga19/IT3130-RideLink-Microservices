# IT3130 RideLink Microservices

Backend microservices system for the IT3130 Application Development group assignment.

## Microservices & Ownership

- **Account Service** (Member 1)
- **Driver & Vehicle Service** (Member 2)
- **Ride Management Service** (Member 3)
- **Fare & Payment Service** (Member 4 - *Currently Active*)

## Prerequisites & Configuration
- Java 17 and Maven
- PostgreSQL running locally (databases: `account_db`, `driver_db`, `ride_db`, `payment_db`)

## Start-up Order
For local testing, the services can be started independently, but for full integrated workflows, they should ideally be started in this order:
1. Account Service (Port 8081)
2. Driver & Vehicle Service (Port 8082)
3. Ride Management Service (Port 8083)
4. Fare & Payment Service (Port 8084)

## API Documentation
Once running, you can access the Swagger UI for each service at:
- `http://localhost:<PORT>/swagger-ui/index.html`
