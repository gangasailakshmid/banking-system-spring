# Kafka integration

The Spring Boot app runs the producer and the activation consumer in the same process. PostgreSQL stores bank status and pending Kafka events. Kafka carries activation requests and outcomes. The customer grid displays the saved PostgreSQL status when it loads or when you select **Reload**.

## Local setup

Start your Docker Kafka broker, then run `./run.sh` from the repository root. The broker must advertise an address reachable from the host where Spring Boot runs. The app declares both topics at startup with one partition and one replica, suitable for the local single-broker setup.

| Environment variable in `.env` | Default |
| --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `KAFKA_ACTIVATION_GROUP` | `banking-system-activation` |
| `KAFKA_ACTIVATION_REQUEST_TOPIC` | `customer-bank-activation-requests` |
| `KAFKA_ACTIVATION_RESULT_TOPIC` | `customer-bank-activation-results` |

The [Maven configuration](pom.xml) includes `spring-boot-starter-kafka`. [Application properties](src/main/resources/application.properties#L23) configure string serializers and deserializers, producer acknowledgments, and record-level consumer offset commits with auto-commit disabled.

## From registration to activation

1. [RegistrationRepository.saveAndFlush()](src/main/java/com/example/banking/RegistrationRepository.java#L63) saves personal information in MySQL and the address in MongoDB. It then passes the bank details to the activation service.
2. [BankActivationService.savePending()](src/main/java/com/example/banking/bank/BankActivationService.java#L25) generates an activation request UUID, sets the bank status to `IN_PROGRESS`, and writes the bank details and request event in one PostgreSQL transaction.
3. [BankActivationMessaging.publishPendingEvents()](src/main/java/com/example/banking/bank/BankActivationMessaging.java#L61) polls the outbox with a one-second delay between runs. It publishes each event using `KafkaTemplate.send()` and deletes the outbox row after the broker acknowledges the send.
4. [BankActivationMessaging.consumeRequest()](src/main/java/com/example/banking/bank/BankActivationMessaging.java#L81) uses `@KafkaListener` to read requests. It parses the JSON and passes the customer ID and request UUID to the activation service.
5. [BankActivationService.activate()](src/main/java/com/example/banking/bank/BankActivationService.java#L33) locks the bank row in PostgreSQL. It processes the request when the row is still `IN_PROGRESS` and the UUID matches the latest save. It sets the final status and writes the result event in the same PostgreSQL transaction. The scheduled publisher then delivers it to the results topic.

## Bank rules and message examples

| Bank | Required account prefix |
| --- | --- |
| Bank of America | `9898` |
| Chase | `7878` |
| US Bank | `6868` |

Bank names are trimmed and matched case-insensitively. Matching accounts become `ACTIVATED`. Unsupported banks and mismatched prefixes become `FAILED`. Saving corrected details starts a new attempt with a new UUID. Existing bank rows start at `INACTIVE` until saved.

Both topics use the string customer ID as their Kafka record key. These example records share the same request ID so they can be correlated.

**Request on `customer-bank-activation-requests`**

```json
{"customerId":42,"requestId":"16e2ff06-00c1-4c15-b729-fc04b5cad65c"}
```

**Result on `customer-bank-activation-results`**

```json
{"customerId":42,"requestId":"16e2ff06-00c1-4c15-b729-fc04b5cad65c","status":"ACTIVATED"}
```

A failed result has the same structure with `"status":"FAILED"`. The payloads contain only the fields shown. See the [message records](src/main/java/com/example/banking/bank/BankActivationMessaging.java#L95).

## Retries and verification

- **Publish failure.** The outbox row stays in PostgreSQL and the scheduler retries it. A delayed request leaves the bank row in progress. A delayed result can leave the grid showing the final status before the result appears in Kafka.
- **Redelivery.** An acknowledgment followed by an app failure before outbox deletion can cause duplicate publication. The listener ignores requests already processed, superseded by a later edit, or associated with a deleted bank row. Downstream consumers can deduplicate results by `requestId`.
- **Consumer errors.** The error handler retries processing failures with a fixed delay. It skips malformed activation requests. A business-rule failure is processed normally and produces a `FAILED` event.

To verify locally, save two synthetic Bank of America registrations with account numbers starting with `9898` and `7878`. Reload the customer grid and confirm **Activated** and **Failed**. In Kafka UI, look up each customer ID on both topics and compare the request UUID and result status. Run `./mvnw test` for the automated prefix, duplicate-request, and stale-request checks. [Activation tests](src/test/java/com/example/banking/bank/BankActivationServiceTest.java)

See the [database integration guide](DATABASE_INTEGRATION_README.md) for storage ownership and transaction boundaries.
