# Waddle

A 1v1 Wordle battle game. 
Backend in Java / Spring Boot, with JWT auth, PostgreSQL and a small browser UI.

**Live:** [waddle-production-6f43.up.railway.app](https://waddle-production-6f43.up.railway.app) · **API docs:** [/swagger-ui.html](https://waddle-production-6f43.up.railway.app/swagger-ui.html)

## Features
- Sign up / sign in with JWT authentication (passwords hashed with BCrypt)
- Friends: send, accept or reject friend requests
- Challenge a friend to a match; the request waits up to 2 minutes for an answer (long polling)
- Same word for both players, 6 guesses each, a 6-minute match deadline
- When one player finishes, the other gets 1 more minute(maxxed at og deadline)
- Live match status: the opponent's guess count (never their letters)
- Admin-only endpoint to add words to the word list
- Scheduled cleanup jobs for expired matches and old requests

## Tech stack
Java 21 · Spring Boot 4 · Spring Security · Spring Data JPA (Hibernate) · PostgreSQL · Flyway · jjwt · springdoc (Swagger) · Docker · Railway · JUnit 5 · Mockito

## Architecture
Layered: **Controller → Service → Repository → PostgreSQL**. Controllers only handle HTTP, services hold the game rules, and repositories talk to the database.

[InitalDesign.excalidraw](InitalDesign.excalidraw) (no Excalidraw extension -> open the file on [excalidraw.com](https://excalidraw.com))

## Win rules
1. Solved beats not solved.
2. Both solved: fewer guesses wins; if equal, the earlier finish time wins; if equal, it's a tie.
3. Neither solved: tie.
4. When one player finishes, the opponent gets 1 more minute, never past the 6-minute match deadline.

## API
Every endpoint, with its request and response shapes, is in Swagger at `/swagger-ui.html`.

## Run it locally

### Option 1: Docker (easiest)
You only need Docker. This builds the app and starts it together with PostgreSQL:

```bash
docker compose up --build
```

To stop it and delete everything it created (containers, database, image):

```bash
docker compose down -v --rmi local
```

### Option 2: Without Docker for the app
You need Java 21 and a PostgreSQL database.

```bash
# 1. Start PostgreSQL
docker run --name waddle-db -e POSTGRES_DB=waddle -e POSTGRES_PASSWORD=secret -p 5432:5432 -d postgres:17

# 2. Start the app (Flyway creates the tables on first start)
./mvnw spring-boot:run
```

Either way, open `http://localhost:8080` for the game, or `http://localhost:8080/swagger-ui.html` for the API docs.


## Tests
Unit tests for the match winner logic (JUnit 5 + Mockito + AssertJ). They mock the repositories.

```bash
./mvnw test -Dtest=GameServiceTests
```


## Credits
The backend was written fully by me. The browser UI (`src/main/resources/static`) was written with Claude