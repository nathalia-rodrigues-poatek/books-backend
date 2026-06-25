# books-backend

A small REST API for managing a library of **books** and their **users**, built with Spring Boot and Kotlin. It covers user management with authentication (BCrypt-hashed passwords, soft delete, block/unblock) and a books catalog (books, authors, genres, publishers) seeded from the public [Open Library](https://openlibrary.org) API on startup.

## Tech stack

- **Kotlin** 2.1.20 (JVM 21)
- **Spring Boot** 3.5.4 — Web, Data JPA, Validation
- **Spring Security Crypto** — BCrypt password hashing
- **H2** — in-memory database (development)
- **Gradle** (Kotlin DSL) with the wrapper
- **JUnit 5** + **mockito-kotlin** for tests

## Requirements

- **JDK 21**

> This project ships with the Gradle wrapper, so no local Gradle install is needed.
>
> If you don't have a system JDK, you can point `JAVA_HOME` at the JBR 21 bundled with Android Studio:
> ```bash
> export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
> ```

## Running the app

```bash
./gradlew bootRun
```

The app starts on **http://localhost:8080**.

On startup, `BookDataSeeder` populates the (empty) database with a handful of real books fetched from the Open Library Search API, so the read endpoints have data to serve immediately. If Open Library is unreachable, seeding is skipped and startup continues normally.

### Database

The app uses an in-memory H2 database (`jdbc:h2:mem:booklubies`) that is recreated on every start.

- **H2 console:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:booklubies`
- **User:** `sa` — **Password:** *(empty)*

## API

Base URL: `http://localhost:8080`

### Users — `/api/users`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/users` | List active (non-deleted) users |
| `GET` | `/api/users/{id}` | Get a user by id |
| `POST` | `/api/users` | Create a user (201) |
| `PUT` | `/api/users/{id}` | Update a user (password optional) |
| `DELETE` | `/api/users/{id}` | Soft delete a user (204) |
| `POST` | `/api/users/{id}/restore` | Restore a soft-deleted user |
| `POST` | `/api/users/{id}/block` | Block a user |
| `POST` | `/api/users/{id}/unblock` | Unblock a user |
| `POST` | `/api/users/login` | Authenticate and return the user token |

Passwords are stored BCrypt-hashed and never returned; the `token` is omitted from user responses (and only returned on login).

### Books — `/api/books`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/books` | List books |
| `GET` | `/api/books/{id}` | Get a book by id |
| `POST` | `/api/books` | Create a book (201) |
| `PUT` | `/api/books/{id}` | Update a book |
| `DELETE` | `/api/books/{id}` | Delete a book (204) |

A book references an existing genre, author and publisher by id (`genreId`, `authorId`, `publisherId`).

### Authors, Genres, Publishers

Analogous CRUD endpoints are available at `/api/authors`, `/api/genres` and `/api/publishers`.

### Error responses

| Status | When |
|--------|------|
| `400 Bad Request` | Payload validation failure (returns per-field errors) |
| `401 Unauthorized` | Invalid login credentials |
| `403 Forbidden` | Blocked user attempting to log in |
| `404 Not Found` | Entity (or a referenced entity) not found |
| `409 Conflict` | Duplicate unique value (e.g. user email/token) |

### Trying the API

Ready-to-run request collections are in the project root and can be executed from IntelliJ IDEA / Android Studio's HTTP client:

- [`users.http`](users.http)
- [`books.http`](books.http)

## Testing

```bash
./gradlew test
```

Unit tests mock the repositories (mockito-kotlin) to exercise the service logic in isolation; the user domain additionally has `@SpringBootTest` integration tests driving the controllers via `MockMvc`.

## Project structure

```
src/main/kotlin/com/books
├── config/                 # App-wide config (PasswordConfig)
├── users/                  # Users domain
│   ├── controllers/  dtos/  models/  repositories/  services/
└── books/                  # Books domain
    ├── clients/            # OpenLibraryClient
    ├── config/             # BookDataSeeder
    ├── controllers/  dtos/  models/  repositories/  services/
```
