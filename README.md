# Banking System Spring

Spring Boot 4.0.5, Java 21+, JSP, Spring Data JPA, Spring Data MongoDB and Spring Security. Runs on http://localhost:8081 alongside the original banking-system app on 8080.

Retains the registration dashboard and three-step wizard, directory search, editing, delete confirmation, sensitive-field preservation on edit, responsive layout and six themes. Login is required for all registration pages and operations. The header includes a Log out button.

| Data | Local database | Persistence |
| --- | --- | --- |
| Personal details | MySQL `banking_spring_personal` on 3306 | `PersonalDetailsRepository` / JPA |
| Login accounts | Same MySQL database, `login_accounts` | `LoginAccountRepository` / JPA |
| Address and phone | MongoDB `banking_spring_address` on 27017 | `AddressDetailsRepository` / Spring Data MongoDB |
| Bank details and SSN | PostgreSQL `banking_spring_bank` on 5432 | `BankDetailsRepository` / JPA |

Separate entity manager factories and transaction managers isolate MySQL and PostgreSQL. MySQL generates the registration ID shared by all stores. The combined `Registration` class is a form DTO; the coordinating service joins the three domain entities for the UI. Login accounts and customer registrations are separate: registering a customer does not create a login account. The initial administrator can manage all registrations.

## Local startup

Start local MySQL, PostgreSQL and MongoDB, then:

```sh
psql -h localhost -U postgres -d postgres -c 'CREATE DATABASE banking_spring_bank;'
cp .env.example .env
# Edit .env with local DB credentials and an initial ADMIN_PASSWORD (12+ characters).
./run.sh
```

For the current workspace, `.env` already contains working local database credentials and a generated administrator password. Open http://localhost:8081/login. Use the `ADMIN_USERNAME` and `ADMIN_PASSWORD` values in `.env`. The file is ignored by Git and readable only by your user.

MySQL creates the new database when the configured user has permission. Hibernate creates/updates the JPA tables; MongoDB creates its collection on the first registration. Existing banking-system data is untouched. If the Homebrew MongoDB service is unavailable, the local MongoDB instance previously started for banking-system can serve both apps using their separate databases.

Configuration overrides: `MYSQL_URL`, `MYSQL_USERNAME`, `MYSQL_PASSWORD`, `POSTGRES_URL`, `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`, `MONGO_URI`, `MONGO_DATABASE`, `PORT`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`. Defaults point to local services. Stop the foreground application with Ctrl+C.

## Login and logout

The administrator is created once if that username does not exist. Passwords are BCrypt hashed in MySQL; `.env` supplies only the initial bootstrap password. Changing `ADMIN_PASSWORD` later does not reset an existing account. To reset a local demo account, stop the app, delete only that username from `login_accounts`, update `.env`, and restart.

Spring Security handles authentication, session fixation protection, and CSRF protection. Every POST form includes a CSRF token. Logout uses POST, invalidates the session, and deletes the app-specific `BANKING_SPRING_SESSION` cookie. Expired or invalid CSRF requests show a readable refresh page and perform no mutation. No default password or public self-signup endpoint is provided.

## Verification

```sh
./mvnw verify
# With ./run.sh active and .env configured, performs synthetic local integration checks:
python3 scripts/verify-local.py
```

Automated tests cover authentication redirects, valid/invalid passwords, CSRF rejection, authenticated saving, logout, persistence mapping, joins and failure compensation. The HTTP verification script additionally checks both registration flows, actual records in MySQL/PostgreSQL/MongoDB, edit preservation, deletion and logout. It deletes only the synthetic registrations it creates. The script requires local `mysql`, `psql` and `mongosh` command-line clients and uses the default banking_spring_* database names.

## Consistency and demo limitations

JPA transactions apply individually to each SQL database. They do not form an atomic transaction with MongoDB or the other SQL database. The service attempts compensating repairs on caught write/delete failures, attaches repair errors as suppressed exceptions and rejects incomplete records on read. Calls are serialized within one app instance; crashes, failed repairs or multiple app instances still require reconciliation by registration ID. A production system needs durable coordination and concurrency controls.

Account numbers and SSNs retain the original app's plaintext storage, so use synthetic details. This is a local registration demo with login, not a production banking platform.

Implementation references: [Spring Data JPA configuration](https://docs.spring.io/spring-data/jpa/reference/repositories/create-instances.html), [Spring Security form login](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/form.html), and [CSRF protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html).
