# Billing and Collections Microservice

This project implements a simplified billing and collections microservice for an insurance domain. It demonstrates service-oriented design, asynchronous processing, retry handling, and clean API design.

---

## Overview

The system supports:

- Retrieving premium schedules for policies
- Recording payment attempts and results
- Identifying delinquent policies
- Asynchronous payment processing with retry logic
- Payment reminders and delinquency tracking (design-level)

---

## Architecture

The system follows a microservice-style architecture with:

- REST API layer
- In-memory persistence (for demonstration)
- Asynchronous processing via queue + worker pattern
- Retry handling using state transitions and scheduling

📄 See full design:
- [Architecture Document](docs/billing-collections-architecture.pdf)

---

## Key Design Decisions

### 1. Separation of Concerns
- Policy/Billing logic and Payment processing are separated conceptually
- Payments are handled asynchronously to improve reliability and scalability

### 2. Asynchronous Processing
- Payments are not processed inline
- A dispatcher + queue + worker model is used:
    - `PaymentDispatcher` finds eligible payments
    - `PaymentQueue` buffers work
    - `PaymentWorker` executes payments

### 3. Retry Strategy
- Failed payments transition to `RETRY_PENDING`
- `nextAttemptAt` controls retry timing
- Dispatcher re-enqueues eligible retries
- Retry logic is fully automated (no manual endpoint required)

### 4. Database as Source of Truth
- Queue only carries `paymentId`
- All state lives in the database
- Ensures recoverability and consistency

### 5. Mock Payment Processor
- First attempt fails
- Second attempt succeeds
- Enables deterministic testing of retry logic

## API Endpoints

### Create Payment

POST /payments

Creates a payment and schedules it for processing.

#### Request

```json
{
  "policyId": "POLICY-1001",
  "scheduleItemId": "uuid",
  "amount": 120.00
}
```

#### Response

```json
{
  "paymentId": "uuid",
  "status": "PENDING"
}
```

---

### Get Premium Schedule

GET /policies/{policyId}/premium-schedule

Returns all scheduled premium payments for a policy.

---

### Get Delinquent Policies

GET /policies/delinquent

Returns policies currently marked as delinquent.

---

## Payment Processing Flow

1. Payment is created (PENDING)
2. Dispatcher enqueues eligible payments
3. Worker processes payment
4. On failure:
    - Status → RETRY_PENDING
    - nextAttemptAt is set
5. Dispatcher re-enqueues when ready
6. Second attempt succeeds → SUCCEEDED

---

## Running the Application

./gradlew bootRun

App runs at:
http://localhost:8080

---

## Running Tests

./gradlew test

Test coverage includes:

- Worker / dispatcher logic
- Retry behavior
- Service layer
- Controller endpoints
- End-to-end payment flow

---

## End-to-End Test

Validates:

- Payment submission
- First attempt failure
- Automatic retry
- Second attempt success
- Correct recording of attempts

---

## Assumptions

- In-memory storage used
- External payment processor mocked
- Fixed retry count (2 attempts)
- Simple scheduling via @Scheduled

---

## Future Improvements

- Replace in-memory storage with database
- Use Kafka or RabbitMQ instead of in-memory queue
- Add idempotency keys
- Implement exponential backoff
- Add authentication/authorization
- Externalize configuration

---

## Summary

This project demonstrates:

- Asynchronous processing
- Reliable retry handling
- Separation of responsibilities