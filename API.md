# Library Core API

Base URL:

```text
http://localhost:8080/api
```

All library APIs require JWT auth except:

- `POST /api/auth/bootstrap`
- `POST /api/auth/login`
- Swagger and health endpoints

Send token:

```http
Authorization: Bearer <accessToken>
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health:

```text
http://localhost:8080/actuator/health
```

## Pagination

List APIs return Spring `Page<T>`.

Query params:

| Param | Default | Max | Example |
| --- | ---: | ---: | --- |
| `page` | `0` | - | `page=0` |
| `size` | `20` | `100` | `size=10` |
| `sort` | - | - | `sort=title,asc` |

## Error Response

```json
{
  "timestamp": "2026-05-16T15:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/books",
  "fieldErrors": {
    "title": "must not be blank"
  }
}
```

Common statuses:

| Status | Meaning |
| ---: | --- |
| `400` | validation/business request error |
| `401` | missing/invalid token or bad credentials |
| `403` | authenticated user lacks required role |
| `404` | resource not found |
| `409` | duplicate, DB constraint, or concurrent update conflict |
| `500` | unexpected server error |

## Auth

Roles:

| Role | Access |
| --- | --- |
| `ADMIN` | all APIs, can create users |
| `LIBRARIAN` | library APIs, cannot create users |

### Bootstrap First Admin

```http
POST /api/auth/bootstrap
Content-Type: application/json
```

Creates first admin only when zero users exist.

Request:

```json
{
  "fullName": "Admin User",
  "email": "admin@example.com",
  "password": "password123",
  "role": "ADMIN"
}
```

Response: `201 Created`, `UserResponse`

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "admin@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresAt": "2026-05-16T17:30:00Z",
  "userId": 1,
  "email": "admin@example.com",
  "fullName": "Admin User",
  "role": "ADMIN"
}
```

### Create User

```http
POST /api/auth/users
Authorization: Bearer <admin-token>
Content-Type: application/json
```

Admin only.

Request:

```json
{
  "fullName": "Library Staff",
  "email": "staff@example.com",
  "password": "password123",
  "role": "LIBRARIAN"
}
```

Response: `201 Created`, `UserResponse`

## Books

### List Books

```http
GET /api/books?search=clean&page=0&size=20&sort=title,asc
```

Response: `Page<BookResponse>`

```json
{
  "content": [
    {
      "id": 1,
      "title": "Clean Code",
      "author": "Robert Martin",
      "isbn": "9780132350884",
      "category": "Software",
      "totalCopies": 2,
      "availableCopies": 2,
      "shelfLocation": "A1",
      "createdAt": "2026-05-16T15:30:00Z",
      "updatedAt": "2026-05-16T15:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Get Book

```http
GET /api/books/{id}
```

Response: `BookResponse`

### Create Book

```http
POST /api/books
Content-Type: application/json
```

Request:

```json
{
  "title": "Clean Code",
  "author": "Robert Martin",
  "isbn": "9780132350884",
  "category": "Software",
  "totalCopies": 2,
  "availableCopies": 2,
  "shelfLocation": "A1"
}
```

Response: `201 Created`, `BookResponse`

Validation:

| Field | Rule |
| --- | --- |
| `title` | required, max 255 |
| `author` | required, max 255 |
| `isbn` | required, unique, max 32 |
| `category` | optional, max 120 |
| `totalCopies` | min 0 |
| `availableCopies` | min 0, cannot exceed `totalCopies` |
| `shelfLocation` | optional, max 120 |

### Update Book

```http
PUT /api/books/{id}
Content-Type: application/json
```

Request: same as create.

Response: `BookResponse`

### Delete Book

```http
DELETE /api/books/{id}
```

Response: `204 No Content`

Notes:

- Delete is soft delete.
- Delete blocked when book has active loans.

## Members

### List Members

```http
GET /api/members?search=ada&page=0&size=20
```

Response: `Page<MemberResponse>`

### Get Member

```http
GET /api/members/{id}
```

Response:

```json
{
  "id": 1,
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "status": "ACTIVE",
  "createdAt": "2026-05-16T15:30:00Z",
  "updatedAt": "2026-05-16T15:30:00Z"
}
```

### Create Member

```http
POST /api/members
Content-Type: application/json
```

Request:

```json
{
  "name": "Ada Lovelace",
  "email": "ada@example.com",
  "status": "ACTIVE"
}
```

Response: `201 Created`, `MemberResponse`

Validation:

| Field | Rule |
| --- | --- |
| `name` | required, max 255 |
| `email` | required, valid email, unique, max 255 |
| `status` | required: `ACTIVE` or `INACTIVE` |

### Update Member

```http
PUT /api/members/{id}
Content-Type: application/json
```

Request: same as create.

Response: `MemberResponse`

### Deactivate Member

```http
PATCH /api/members/{id}/deactivate
```

Response: `MemberResponse`

## Loans

### Dashboard

```http
GET /api/dashboard
```

Response:

```json
{
  "books": 10,
  "members": 5,
  "activeLoans": 2,
  "overdueLoans": 1,
  "outstandingFines": 25.00
}
```

### Issue Book

```http
POST /api/loans
Content-Type: application/json
```

Request:

```json
{
  "bookId": 1,
  "memberId": 1,
  "dueDate": "2026-05-30"
}
```

Response: `201 Created`, `LoanResponse`

Validation:

| Field | Rule |
| --- | --- |
| `bookId` | required, active book |
| `memberId` | required, active member |
| `dueDate` | optional, today/future date |

Rules:

- If `dueDate` omitted, default due date = today + configured loan days.
- Member cannot issue same book twice while active loan exists.
- Book must have available copies.

### Return Book

```http
PATCH /api/loans/{id}/return
```

Response: `LoanResponse`

Rules:

- Already returned loan returns `400`.
- Returning increments book available copies.

### Loan History

```http
GET /api/loans?page=0&size=20
GET /api/loans?memberId=1
GET /api/loans?bookId=1
```

Response: `Page<LoanResponse>`

```json
{
  "content": [
    {
      "id": 1,
      "bookId": 1,
      "bookTitle": "Clean Code",
      "bookIsbn": "9780132350884",
      "memberId": 1,
      "memberName": "Ada Lovelace",
      "memberEmail": "ada@example.com",
      "issuedAt": "2026-05-16T15:30:00Z",
      "dueDate": "2026-05-30",
      "returnedAt": null,
      "status": "ISSUED",
      "overdueDays": 0,
      "fineAmount": 0.00
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

Loan statuses:

| Status | Meaning |
| --- | --- |
| `ISSUED` | active loan, not overdue |
| `OVERDUE` | active loan past due date, computed in response |
| `RETURNED` | returned loan |

Fine calculation:

```text
overdueDays = max(0, current-or-return-date - dueDate)
fineAmount = overdueDays * app.library.overdue-fine-per-day
```

Default fine per day:

```properties
app.library.overdue-fine-per-day=5.00
```

## Audit Logs

Admin only.

```http
GET /api/audit-logs?page=0&size=20&sort=createdAt,desc
Authorization: Bearer <admin-token>
```

Response: `Page<AuditLogResponse>`

```json
{
  "content": [
    {
      "id": 1,
      "actorEmail": "admin@example.com",
      "action": "ISSUE_LOAN",
      "entityType": "Loan",
      "entityId": "10",
      "details": "bookId=1,memberId=2,dueDate=2026-05-30",
      "createdAt": "2026-05-22T12:30:00Z"
    }
  ]
}
```
