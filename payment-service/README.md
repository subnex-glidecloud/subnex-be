# Payment Service 💳

## Overview

The Payment Service handles all payment processing for the subscription system. It simulates Stripe-like behavior in test mode and publishes payment events to Kafka for the rest of the system to consume.

## Architecture

```
User → Subscription Service → Payment Service → Stripe (Mock)
                                         ↓
                                  MongoDB (payments)
                                         ↓
                                  Kafka (payment-events)
                                         ↓
                          Email Service + Subscription Updates
```

## Responsibilities

### ✅ What It DOES

- Create payment attempts for initial and renewal subscriptions
- Mock Stripe PaymentIntent behavior (success/failure randomized)
- Store all payment records in MongoDB
- Publish payment events to Kafka (`PAYMENT_SUCCESS`, `PAYMENT_FAILED`)
- Provide REST API for payment inquiries

### ❌ What It DOES NOT Do

- Authenticate users (auth-service handles this)
- Manage subscription lifecycle (subscription-service handles this)
- Send emails directly (email-service listens to events)
- Store real card data (Stripe handles this)

## API Endpoints

### Initiate Payment

```bash
POST /api/payments/initiate
Content-Type: application/json

{
  "subscriptionId": "sub_123",
  "userId": "user_123",
  "amount": 999,
  "currency": "INR",
  "type": "RENEWAL"
}

# Response (201 Created)
{
  "id": "pay_abc123",
  "subscriptionId": "sub_123",
  "userId": "user_123",
  "amount": 999,
  "currency": "INR",
  "status": "SUCCESS",
  "type": "RENEWAL",
  "attempt": 1,
  "createdAt": "2026-01-19T10:00:00Z",
  "updatedAt": "2026-01-19T10:00:00Z"
}
```

### Get Payment Status

```bash
GET /api/payments/{paymentId}

# Response (200 OK)
{
  "id": "pay_abc123",
  "subscriptionId": "sub_123",
  "status": "SUCCESS",
  ...
}
```

## Kafka Events

### Topic: `payment-events`

#### Payment Success Event

```json
{
  "eventType": "PAYMENT_SUCCESS",
  "subscriptionId": "sub_123",
  "userId": "user_123",
  "amount": 999,
  "currency": "INR",
  "timestamp": "2026-01-19T10:00:00Z"
}
```

#### Payment Failed Event

```json
{
  "eventType": "PAYMENT_FAILED",
  "subscriptionId": "sub_123",
  "userId": "user_123",
  "amount": 999,
  "currency": "INR",
  "reason": "Insufficient funds",
  "timestamp": "2026-01-19T10:00:00Z"
}
```

## Database Schema

### Payments Collection

```json
{
  "_id": "ObjectId",
  "subscriptionId": "sub_123",
  "userId": "user_123",
  "stripePaymentIntentId": "pi_abc123",
  "amount": 999,
  "currency": "INR",
  "status": "SUCCESS",
  "type": "RENEWAL",
  "attempt": 1,
  "failureReason": null,
  "createdAt": "2026-01-19T10:00:00Z",
  "updatedAt": "2026-01-19T10:00:00Z"
}
```

## Configuration

### Environment Variables (`.env`)

```env
SERVER_PORT=8083
MONGODB_URI=mongodb://localhost:27017/subnex-payments
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
PAYMENT_SUCCESS_RATE=0.8
STRIPE_SECRET_KEY=sk_test_xxx
STRIPE_WEBHOOK_SECRET=whsec_xxx
```

### application.yml

```yaml
server:
  port: ${SERVER_PORT:8083}

spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}

payment:
  success-rate: ${PAYMENT_SUCCESS_RATE:0.8}
```

## Mock Payment Logic

Payments are simulated with configurable success rate:

```java
boolean success = Math.random() < successRate;
// 80% success rate by default (PAYMENT_SUCCESS_RATE=0.8)
// 20% failure rate
```

This allows testing of both success and failure flows deterministically.

## Testing the Service

### Test Initial Payment

```bash
curl -X POST http://localhost:8083/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": "sub_test_001",
    "userId": "user_test_001",
    "amount": 999,
    "currency": "INR",
    "type": "INITIAL"
  }'
```

### Test Payment Renewal

```bash
curl -X POST http://localhost:8083/api/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "subscriptionId": "sub_test_002",
    "userId": "user_test_001",
    "amount": 999,
    "currency": "INR",
    "type": "RENEWAL"
  }'
```

### Verify in Kafka

```bash
# Check payment-events topic
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic payment-events \
  --from-beginning
```

## Payment Flows

### 1. Initial Subscription Payment

```
User subscribes
  ↓
Subscription Service calls /api/payments/initiate
  ↓
Payment Service creates PaymentIntent
  ↓
Mock success/failure
  ↓
Kafka event published
  ↓
Subscription activated/failed
  ↓
Email sent
```

### 2. Renewal Payment (Scheduled)

```
@Scheduled Job (Subscription Service)
  ↓
Payment Service /api/payments/initiate
  ↓
Stripe PaymentIntent (mocked)
  ↓
Payment Service publishes event
  ↓
Kafka → payment-events
  ↓
Subscription renewed OR marked PAST_DUE
  ↓
Email notification sent
```

## Package Structure

```
com.subnex.payment
├── PaymentServiceApplication.java
├── controller/
│   └── PaymentController.java
├── service/
│   └── PaymentProcessorService.java
├── repository/
│   └── PaymentRepository.java
├── model/
│   └── Payment.java
├── kafka/
│   └── PaymentEventProducer.java
├── dto/
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   └── PaymentEvent.java
├── enums/
│   ├── PaymentStatus.java
│   └── PaymentType.java
└── config/
    └── KafkaProducerConfig.java
```

## Running the Service

### Build

```bash
./gradlew clean build
```

### Run

```bash
./gradlew bootRun
```

### With Environment Variables

```bash
export MONGODB_URI=mongodb://localhost:27017/subnex-payments
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export PAYMENT_SUCCESS_RATE=0.8
./gradlew bootRun
```

## Integration Points

| Service              | Interaction                  |
| -------------------- | ---------------------------- |
| Auth Service         | ❌ None                      |
| Subscription Service | Calls /api/payments/initiate |
| Email Service        | Listens to payment-events    |
| Stripe               | Webhook source (future)      |
| Kafka                | Event transport              |

## Security

- Payment service is internal-only (no public exposure)
- Stripe credentials stored in environment variables
- MongoDB credentials in connection string
- Kafka bootstrap servers configurable

## Future Enhancements

1. **Real Stripe Integration**: Replace mock logic with actual Stripe SDK
2. **Webhook Handling**: Receive `payment_intent.succeeded` and `payment_intent.payment_failed`
3. **Retry Logic**: Exponential backoff for failed payments
4. **Idempotency**: Prevent duplicate payments
5. **Analytics**: Payment success rates, revenue tracking
6. **Admin Dashboard**: Payment history and refunds
7. **Multi-currency**: Support for different currencies
8. **Tax Handling**: VAT/GST calculations

## Technical Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.1
- **Database**: MongoDB
- **Message Broker**: Apache Kafka
- **Build Tool**: Gradle
- **Containerization**: Docker (upcoming)

## License

MIT
