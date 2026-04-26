# Blog App

A RESTful blog application built with **Spring Boot 3.3**, **Spring Security**, and **JWT authentication**. It supports user registration, post management, and ownership-based access control.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Configuration & Profiles](#configuration--profiles)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)

---

## Tech Stack

| Technology                  | Version   | Purpose                              |
|-----------------------------|-----------|--------------------------------------|
| Java                        | 21        | Programming language                 |
| Spring Boot                 | 3.3.9     | Application framework                |
| Spring Security             | (Boot)    | Authentication & authorization       |
| Spring Data JPA             | (Boot)    | Database access (ORM)                |
| JJWT                        | 0.11.5    | JWT token generation & validation    |
| MapStruct                   | 1.6.3     | DTO ↔ Entity mapping                 |
| Lombok                      | (Boot)    | Boilerplate code reduction           |
| H2                          | (Boot)    | In-memory database (dev/test)        |
| PostgreSQL                  | 16        | Relational database (production)     |
| SpringDoc OpenAPI (Swagger) | 2.3.0     | API documentation UI                 |
| Spring Boot Actuator        | (Boot)    | Health & monitoring endpoints        |
| JaCoCo                      | 0.8.12    | Code coverage reporting              |
| Docker Compose              | —         | PostgreSQL + Adminer local setup     |

---

## Project Structure

```
src/main/java/com/kte/blog_app/
├── BlogAppApplication.java          # Application entry point
├── config/
│   ├── SecurityConfig.java          # Spring Security configuration
│   ├── SwaggerConfig.java           # OpenAPI/Swagger configuration
│   └── DataInitializer.java         # Seed data on startup
├── controllers/
│   ├── AuthController.java          # POST /api/v1/auth/register & /login
│   ├── PostController.java          # CRUD for posts
│   ├── UserController.java          # User read/update/delete
│   └── ui_controllers/              # Controller interfaces (Swagger annotations)
├── domain/
│   ├── entities/
│   │   ├── User.java                # User JPA entity
│   │   ├── Post.java                # Post JPA entity
│   │   └── PostStatus.java          # Enum: DRAFT | PUBLISHED
│   └── dto/
│       ├── request/                 # Incoming request DTOs
│       └── response/                # Outgoing response DTOs
├── exceptions/
│   ├── PostNotFoundException.java
│   ├── UserNotFoundException.java
│   ├── UserAlreadyExistsException.java
│   └── handler/                     # Global exception handler (@RestControllerAdvice)
├── mappers/
│   ├��─ PostMapper.java              # MapStruct Post ↔ DTO
│   └── UserMapper.java              # MapStruct User ↔ DTO
├── repositories/
│   ├── PostRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java # JWT filter (Bearer token extraction)
│   ├── BlogUserDetails.java         # Custom UserDetails implementation
│   ├── AuthorizationService.java    # Generic authorization checks
│   ├── PostSecurityService.java     # Post-level access rules
│   └���─ UserSecurityService.java     # User-level access rules
└── services/
    ├── AuthenticationService.java   # Register, login, token generation
    ├── PostService.java             # Post business logic interface
    ├── UserService.java             # User business logic interface
    └── impl/                        # Service implementations
```

---

## Domain Model

### User

| Field      | Type          | Constraints               |
|------------|---------------|---------------------------|
| id         | Long          | Primary key, auto-gen     |
| email      | String        | Unique, not null          |
| password   | String        | Not null (BCrypt encoded) |
| name       | String        | Not null                  |
| createDate | LocalDateTime | Set on creation           |
| posts      | List\<Post\>  | One-to-many (cascade ALL) |

### Post

| Field      | Type          | Constraints              |
|------------|---------------|--------------------------|
| id         | Long          | Primary key, auto-gen    |
| title      | String        | Not null                 |
| content    | String (TEXT) | Not null                 |
| author     | User          | Many-to-one, not null    |
| category   | PostStatus    | Enum: DRAFT / PUBLISHED  |
| createDate | LocalDateTime | Set on persist           |
| updateDate | LocalDateTime | Updated on each save     |

---

## API Endpoints

### Authentication — `/api/v1/auth`

| Method | Path        | Auth Required | Description              |
|--------|-------------|---------------|--------------------------|
| POST   | `/register` | No            | Register a new user      |
| POST   | `/login`    | No            | Login and receive a JWT  |

**Register — request body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

**Login — request body:**
```json
{
  "email": "john@example.com",
  "password": "secret123"
}
```

**Auth response (both endpoints):**
```json
{
  "token": "<JWT>",
  "expiresIn": 86400
}
```

---

### Posts — `/api/v1/posts`

| Method | Path                              | Auth Required  | Description                        |
|--------|-----------------------------------|----------------|------------------------------------|
| POST   | `/`                               | ✅ Yes         | Create a post (authenticated user) |
| GET    | `/{id}`                           | No             | Get a post by ID                   |
| GET    | `/category?category=`             | No             | Get posts filtered by category     |
| GET    | `/search?authorId=&category=`     | No             | Get posts by author and category   |
| PUT    | `/{id}`                           | ✅ Owner only  | Update a post                      |
| DELETE | `/{id}`                           | ✅ Owner only  | Delete a post                      |

**PostStatus values:** `DRAFT`, `PUBLISHED`

---

### Users — `/api/v1/user`

| Method | Path              | Auth Required  | Description         |
|--------|-------------------|----------------|---------------------|
| GET    | `/{id}`           | No             | Get user by ID      |
| GET    | `/email/{email}`  | No             | Get user by email   |
| PUT    | `/{id}`           | ✅ Owner only  | Update user profile |
| DELETE | `/{id}`           | ✅ Owner only  | Delete user account |

---

## Security

- **Authentication:** JWT Bearer tokens passed via the `Authorization` header (`Bearer <token>`).
- **Token expiration:** 24 hours (86 400 000 ms).
- **JWT secret:** Read from the `JWT_SECRET` environment variable.
- **Filter:** `JwtAuthenticationFilter` runs on every request, validates the token, and populates the Spring Security context.
- **Method-level authorization:**
  - `PostSecurityService` — only the post owner can `PUT` or `DELETE` a post (`@PreAuthorize`).
  - `UserSecurityService` — only the account owner can `PUT` or `DELETE` a user.

---

## Configuration & Profiles

### `application.yaml` (default profile: `dev`)

- H2 in-memory database (`jdbc:h2:mem:blogforumdb`)
- JWT secret from `$JWT_SECRET`
- Actuator exposes `health` and `info`

### `application-dev.yaml`

- H2 in-memory, DDL strategy: `create-drop`
- H2 Console enabled at `/h2-console`
- Server port: `8888` (override with `$SERVER_PORT`)
- Verbose DEBUG/TRACE logging

### `application-prod.yaml`

- PostgreSQL via `$DB_URL` / `$DB_USERNAME` / `$DB_PASSWORD`
- DDL strategy: `validate` (schema must already exist)
- Server port: `8181` (override with `$SERVER_PORT`)
- Log file: `logs/blog-app.log` (10 MB max, 30-day retention)

### Required environment variables

| Variable      | Profile       | Description                    |
|---------------|---------------|--------------------------------|
| `JWT_SECRET`  | All           | JWT HMAC signing secret        |
| `DB_URL`      | prod          | PostgreSQL JDBC URL            |
| `DB_USERNAME` | prod / Docker | Database username              |
| `DB_PASSWORD` | prod / Docker | Database password              |
| `SERVER_PORT` | Optional      | Overrides the default port     |

---

## Running the Application

### Development — H2 in-memory database

```powershell
$env:JWT_SECRET = "your-very-secret-key"
./mvnw spring-boot:run
```

| URL | Description |
|-----|-------------|
| `http://localhost:8888` | REST API base URL |
| `http://localhost:8888/swagger-ui.html` | Swagger UI |
| `http://localhost:8888/h2-console` | H2 Database Console |

---

### Production — PostgreSQL via Docker

**1. Create a `.env` file at the project root:**

```env
DB_USERNAME=postgres
DB_PASSWORD=yourpassword
JWT_SECRET=your-very-secret-key
```

**2. Start PostgreSQL and Adminer:**

```powershell
docker-compose up -d
```

**3. Run the application with the `prod` profile:**

```powershell
$env:SPRING_PROFILES_ACTIVE = "prod"
$env:DB_URL      = "jdbc:postgresql://localhost:5432/blogappDB"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "yourpassword"
$env:JWT_SECRET  = "your-very-secret-key"
./mvnw spring-boot:run
```

| URL | Description |
|-----|-------------|
| `http://localhost:8181` | REST API base URL |
| `http://localhost:8989` | Adminer (DB administration UI) |

---

## Running Tests

```powershell
# Run all tests and generate JaCoCo coverage report
./mvnw test
```

After the build, open the coverage report at:

```
target/site/jacoco/index.html
```

The test suite covers: controllers, services, mappers, repositories, security services, and the global exception handler — using **JUnit 5**, **Mockito**, and **Spring Security Test**.

