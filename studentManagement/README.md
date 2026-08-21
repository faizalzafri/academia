# Academia Platform

A web-based academic management system built with Spring Boot for managing students, teachers, courses, timetables, and school calendar events.

## Functional Overview

### User Roles

| Role | Description |
|------|-------------|
| **PRINCIPAL** | Institutional governance: starts/concludes academic years, bulk promotes student cohorts, reviews audits & historical archives |
| **SYSTEM_ADMIN** | Approves/rejects registrations, manages academic structure, technical system administration |
| **TEACHER** | Manages courses & syllabi, uploads study materials, coordinates activities & records results, views timetable & calendar |
| **STUDENT (USER)** | Views enrolled courses, downloads materials, registers for extra-curricular activities, accesses timetable & chatroom |

### Features

- **Academic Year Sessions Lifecycle** — Create upcoming sessions in `PLANNING` state, transition to `ACTIVE`, and conclude with `COMPLETED`/`ARCHIVED` status.
- **Bulk Cohort Promotion & Alumni Tracking** — Automatically promote students to the next grade class across academic sessions, while marking graduating Class 12 students as Alumni.
- **Historical Data Archive** — Audit past sessions' class rosters, student enrollments, and teacher assignments in immutable read-only mode.
- **Principal Governance Dashboard** — High-level institutional metrics, session audit reports, and staff-to-student ratio tracking.
- **Extra-Curricular Activities Subsystem** — Plan, manage, and register for Sports Day, Annual Day, Farewell ceremonies, Prom dances, and House competitions. Record podium winners, medals, and scores.
- **Registration & Approval Workflow** — Role-based onboarding where Student and Teacher accounts require admin approval before activation.
- **Course Management & Syllabi** — Teachers create and manage courses; students browse course catalog and view study resources.
- **Academic Matrix & Timetable Builder** — Class/section assignment with real-time teacher double-booking conflict detection.
- **School Calendar & Real-Time Chatroom** — Institutional calendar events and WebSocket-powered STOMP chatroom.

### Default Admin Credentials

```
Username: admin
Password: Password123!
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 2.7.18 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Hibernate / Spring Data JPA |
| Schema Management | Liquibase |
| Templating | Thymeleaf |
| Security | Spring Security |
| WebSocket | Spring WebSocket + STOMP |
| Testing | JUnit 5, Spring Boot Test, H2 (in-memory) |
| Code Coverage | JaCoCo |
| Build Tool | Maven |

## Project Structure

```
src/main/java/com/academia/platform/
├── config/          # Security, WebSocket, MVC configuration
├── controller/      # REST & MVC controllers
├── dto/             # Data Transfer Objects
├── model/           # JPA entities (User, Student, Teacher, Course, etc.)
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic services
├── validator/       # Custom validators
└── listener/        # Event listeners

src/main/resources/
├── application.properties
├── db/changelog/    # Liquibase changelogs (YAML)
├── templates/       # Thymeleaf HTML templates
└── static/          # CSS, JS, images
```

## Prerequisites

- Java 17+
- PostgreSQL 14+
- Maven 3.8+ (or use the included `mvnw` wrapper)

---

## Maven Commands

### Build & Run

```bash
# Compile the project
./mvnw clean compile

# Run the application
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package

# Package skipping tests
./mvnw clean package -DskipTests

# Run the packaged JAR
java -jar target/academia-platform-0.0.1-SNAPSHOT.jar
```

### Testing

```bash
# Run all tests (unit + integration on H2)
./mvnw clean test

# Run a specific test class
./mvnw test -Dtest=RegistrationAndApprovalIntegrationTest

# Run a specific test method
./mvnw test -Dtest=SecurityAndAccessControlIntegrationTest#testUnauthenticatedUserRedirectedToLogin
```

### Code Coverage (JaCoCo)

```bash
# Generate coverage report (runs tests first)
./mvnw clean test jacoco:report

# View report — open in browser:
# target/site/jacoco/index.html
```

### Liquibase

```bash
# Check migration status
./mvnw liquibase:status -Dliquibase.url=jdbc:postgresql://localhost:5432/academiadb -Dliquibase.username=postgres -Dliquibase.password=postgres

# Run pending migrations manually
./mvnw liquibase:update -Dliquibase.url=jdbc:postgresql://localhost:5432/academiadb -Dliquibase.username=postgres -Dliquibase.password=postgres

# Rollback last changeset
./mvnw liquibase:rollbackCount -Dliquibase.rollbackCount=1 -Dliquibase.url=jdbc:postgresql://localhost:5432/academiadb -Dliquibase.username=postgres -Dliquibase.password=postgres

# Generate diff changelog from current DB
./mvnw liquibase:diff
```

### Dependency & Plugin Info

```bash
# Show dependency tree
./mvnw dependency:tree

# Check for dependency updates
./mvnw versions:display-dependency-updates

# Check for plugin updates
./mvnw versions:display-plugin-updates

# Show effective POM (resolved BOM versions)
./mvnw help:effective-pom
```

---

## PostgreSQL Commands

### Local Installation

```bash
# Start PostgreSQL service (Linux)
sudo systemctl start postgresql

# Start PostgreSQL service (macOS with Homebrew)
brew services start postgresql

# Start PostgreSQL service (Windows)
net start postgresql-x64-14
```

### Create Database

```sql
-- Connect as superuser
psql -U postgres

-- Create the database
CREATE DATABASE academiadb;

-- List all databases
\l

-- Connect to academiadb
\c academiadb
```

### Useful psql Commands (after connecting)

```sql
-- List all tables
\dt

-- Describe a table structure
\d users
\d course
\d class_sections
\d timetable_slots

-- Count rows in a table
SELECT count(*) FROM users;

-- View all registered users
SELECT username, user_type, enabled, approval_status FROM users;

-- View pending registrations
SELECT username, user_type, email, approval_status, registration_date
FROM users
WHERE approval_status = 'PENDING';

-- View all courses
SELECT id, code, name, credit FROM course;

-- View class sections with teachers
SELECT cs.id, sc.classname, s.sectionname, cs.class_teacher_username
FROM class_sections cs
JOIN school_classes sc ON cs.class_id = sc.id
JOIN sections s ON cs.section_id = s.id;

-- View timetable for a class section
SELECT ts.dayofweek, ts.periodnumber, sub.name AS subject, ts.teacher_username, ts.starttime, ts.endtime
FROM timetable_slots ts
JOIN subjects sub ON ts.subject_id = sub.id
WHERE ts.class_section_id = 1
ORDER BY ts.dayofweek, ts.periodnumber;

-- View calendar events
SELECT eventdate, title, type FROM school_calendar_events ORDER BY eventdate;

-- View Liquibase changelog tracking
SELECT id, author, filename, dateexecuted, exectype FROM databasechangelog;

-- Check authorities for a user
SELECT * FROM authorities WHERE users_username = 'admin';

-- Quit psql
\q
```

---

## Docker Commands

### Run PostgreSQL in Docker

```bash
# Pull PostgreSQL image
docker pull postgres:14

# Start PostgreSQL container
docker run -d \
  --name academia-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=academiadb \
  -p 5432:5432 \
  postgres:14

# Check container is running
docker ps

# View container logs
docker logs academia-postgres

# Stop the container
docker stop academia-postgres

# Start it again
docker start academia-postgres

# Remove the container (data lost unless using a volume)
docker rm -f academia-postgres
```

### Run with Persistent Volume

```bash
# Create a named volume
docker volume create academia-pgdata

# Run with volume mount
docker run -d \
  --name academia-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=academiadb \
  -p 5432:5432 \
  -v academia-pgdata:/var/lib/postgresql/data \
  postgres:14
```

### Connect to DB via Docker

```bash
# Open psql shell inside the running container
docker exec -it academia-postgres psql -U postgres -d academiadb

# Once connected, run any psql command:
\dt                          -- list tables
\d users                     -- describe users table
SELECT * FROM users;         -- query data
\q                           -- quit
```

### One-liner: Run a SQL Query via Docker

```bash
# Run a query without entering the shell
docker exec -it academia-postgres psql -U postgres -d academiadb -c "SELECT username, approval_status FROM users;"

# Export query results to a file
docker exec -it academia-postgres psql -U postgres -d academiadb -c "COPY (SELECT * FROM users) TO STDOUT WITH CSV HEADER;" > users_export.csv
```

---

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property | Value | Description |
|----------|-------|-------------|
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/academiadb` | Database connection URL |
| `spring.datasource.username` | `postgres` | DB username |
| `spring.datasource.password` | `postgres` | DB password |
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate validates schema against Liquibase |
| `spring.liquibase.enabled` | `true` | Liquibase runs migrations on startup |

For tests, the app uses an in-memory H2 database configured in `src/test/resources/application.properties`.
