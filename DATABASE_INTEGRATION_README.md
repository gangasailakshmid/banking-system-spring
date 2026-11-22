# Database integration

One customer registration spans three databases. MySQL generates a numeric customer ID, and the app reuses that ID as the PostgreSQL primary key and MongoDB document ID. The application joins the records by customer ID when it loads the directory or the edit form.

## Store ownership and configuration

| Store | Local default | Records and fields |
| --- | --- | --- |
| MySQL | `banking_spring_personal` on port `3306` | `personal_details` holds customer ID, first name, middle name, last name, and gender. `login_accounts` holds administrator usernames and BCrypt password hashes. |
| PostgreSQL | `banking_spring_bank` on port `5432` | `bank_details` holds the customer ID, bank name, account number, SSN, activation status, and latest activation request UUID. `bank_activation_outbox` holds events awaiting Kafka publication. |
| MongoDB | `banking_spring_address` on port `27017` | `address_details` stores one document per customer with the shared ID as `_id`, plus street address, city, state, country, and phone. |

[DatabaseConfiguration.java](src/main/java/com/example/banking/DatabaseConfiguration.java) defines two Hikari data sources, two JPA entity manager factories, and two JPA transaction managers. The MySQL beans are primary and scan the `personal` package. The PostgreSQL beans scan the `bank` package. Each repository group explicitly references its own factory and transaction manager. MongoDB uses a separate `MongoClient` and `MongoTemplate` with repositories in the `address` package.

- **MySQL** uses `PersonalDetailsRepository` and `LoginAccountRepository`. The customer ID comes from the identity column in `personal_details`. The startup initializer creates the administrator if the configured username is missing. Customer saves do not create login accounts. [PersonalDetails.java](src/main/java/com/example/banking/personal/PersonalDetails.java) · [DatabaseInitializer.java](src/main/java/com/example/banking/DatabaseInitializer.java)
- **PostgreSQL** uses `BankDetailsRepository` and `ActivationOutboxRepository`. The bank status is stored as an enum name. The outbox has its own generated row ID, the customer ID, the activation UUID, and an outcome for result events. [BankDetails.java](src/main/java/com/example/banking/bank/BankDetails.java) · [ActivationOutboxEvent.java](src/main/java/com/example/banking/bank/ActivationOutboxEvent.java)
- **MongoDB** uses `AddressDetailsRepository`. The app assigns the document ID from the customer ID so an edit saves to the same document. [AddressDetails.java](src/main/java/com/example/banking/address/AddressDetails.java) · [AddressDetailsRepository.java](src/main/java/com/example/banking/address/AddressDetailsRepository.java)

## Local setup

Start MySQL, PostgreSQL, and MongoDB. Create the PostgreSQL database once if it does not exist.

```sh
psql -h localhost -U postgres -d postgres -c 'CREATE DATABASE banking_spring_bank;'
```

If you do not already have a `.env` file, copy `.env.example` to `.env` and fill in the local SQL credentials and an initial administrator password of at least 12 characters. Start Kafka for the activation flow, then run `./run.sh`.

| Store | Environment overrides |
| --- | --- |
| MySQL | `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD` |
| PostgreSQL | `POSTGRES_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD` |
| MongoDB | `MONGO_URI`, `MONGO_DATABASE` |
| Administrator bootstrap | `ADMIN_USERNAME`, `ADMIN_PASSWORD` |

The default MySQL JDBC URL can create the database when the user has permission. Hibernate uses schema update mode for both SQL persistence units and creates or updates their tables. MongoDB creates the address collection on its first write. The startup initializer pings MongoDB before finishing. Connection defaults are in [application.properties](src/main/resources/application.properties).

## Reads, writes, and failure handling

| Operation | What the application does |
| --- | --- |
| Create | Saves personal details to obtain the ID, saves the MongoDB address under that ID, then commits bank details and the pending activation event in PostgreSQL. |
| Read | Starts with the MySQL personal row, looks up address and bank details by ID, and combines them into the `Registration` form model. Missing address or bank records remain visible and can be filled in from Edit. |
| Edit | Reads the current records for recovery, then saves updated details under the same ID. The dashboard keeps the saved account number and SSN when those edit fields are blank. The bank save starts a new activation attempt. |
| Delete | Takes a snapshot, then deletes the MongoDB address, PostgreSQL bank row, and MySQL personal row in that order. A pending activation request for the removed bank row is ignored by the consumer. |

The registration coordinator serializes its operations within one app instance. MySQL, MongoDB, and PostgreSQL each commit independently. When a save fails, the coordinator attempts to undo earlier writes in MySQL and MongoDB. On an edit, it restores the previous values and removes any newly created address that was absent before the edit. When a delete fails, it attempts to restore the captured records. Recovery errors are attached to the original exception. A crash between stores or a failed recovery can leave partial data that must be reconciled by customer ID. [RegistrationRepository.java](src/main/java/com/example/banking/RegistrationRepository.java#L53)

Within PostgreSQL, `BankActivationService` explicitly uses `bankTransactionManager`. Both the initial bank save and the final status change commit alongside their respective outbox event. The consumer locks the bank row during validation and status update. Kafka delivery runs separately after the database commit. [BankActivationService.java](src/main/java/com/example/banking/bank/BankActivationService.java#L25) · [Kafka integration guide](KAFKA_INTEGRATION_README.md)

For local checks, run `./mvnw test`. With the app running, `python3 scripts/verify-local.py` exercises the registration flows against all three stores using synthetic registrations. It requires the `mysql`, `psql`, and `mongosh` command-line clients. This demo stores account numbers and SSNs in plaintext, so use synthetic data for local checks.
