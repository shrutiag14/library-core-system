# Library Core System

Production-style mini Library Management System.

## Stack

- Backend: Java 21, Spring Boot 3, Spring Data JPA, MySQL, Flyway, OpenAPI
- Frontend: Angular 18, TypeScript, Bootstrap 5

## Backend Production Model

- Layered packages: `controller`, `dto`, `model`, `repository`, `service`, `service.impl`, `exception`, `config`.
- Controllers expose REST contracts only; business rules stay behind service interfaces.
- Service implementations are transaction boundaries with read-only transactions for queries.
- Repositories own persistence queries and use locking for stock-changing workflows.
- Entities share audited fields and optimistic locking through `BaseEntity`.
- Flyway owns schema changes; Hibernate validates schema in normal runtime.
- Config is externalized through environment variables and a stricter `prod` profile.
- API errors are normalized by global exception handling.
- Security is stateless JWT with `ADMIN` and `LIBRARIAN` roles.
- Audit logs capture user/admin/circulation changes.
- Overdue fines are computed from overdue days and configured fine per day.

## Features

- Book CRUD with soft delete, unique ISBN, search, copy validation
- Member create/update/deactivate with unique email
- Issue/return workflow with stock updates, duplicate active-loan protection, overdue status projection
- Transaction history by member or book
- Global API errors and frontend success/error/loading states
- Layered backend: controller, service, repository, DTOs

## Run Backend

```bash
cd backend
DB_URL='jdbc:mysql://localhost:3306/library_core_dev?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
DB_USERNAME=root \
DB_PASSWORD='' \
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`
Health: `http://localhost:8080/actuator/health`

Bootstrap first admin:

```bash
curl -X POST http://localhost:8080/api/auth/bootstrap \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Admin User","email":"admin@example.com","password":"password123","role":"ADMIN"}'
```

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"password123"}'
```

Use returned token:

```bash
curl http://localhost:8080/api/dashboard \
  -H 'Authorization: Bearer <accessToken>'
```

Production profile:

```bash
cd backend
SPRING_PROFILES_ACTIVE=prod \
DB_URL='jdbc:mysql://localhost:3306/library_core_dev?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
DB_USERNAME=root \
DB_PASSWORD='change-me' \
mvn spring-boot:run
```

## Run Frontend

```bash
cd frontend
npm install
npm start
```

App: `http://localhost:4200`

## Tests

```bash
cd backend
mvn test
```

## Assumptions

- No complex authentication required for core assignment.
- Delete is soft delete and blocked when active loans exist.
- Default due date is 14 days when UI/API omits due date.
- Overdue is computed when reading loan history; returned records keep `RETURNED`.
