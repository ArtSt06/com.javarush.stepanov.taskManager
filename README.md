# Task Manager App

RESTful API for managing tasks with JWT authentication and Role-Based Access Control.

## Tech Stack
- **Backend:** Spring Boot 3.5.11, Spring Security, Data JPA
- **Database:** PostgreSQL
- **Migrations:** Liquibase
- **Testing:** JUnit 5, Mockito, MockMvc
- **DevOps:** Docker, Docker Compose, GitHub Actions (CI/CD)

---

## How to Run

Make sure you have Docker installed, then run a single command in your terminal:

```bash
docker compose up --build
```

The server will start on port **`8080`**.  
Healthcheck endpoint: `http://localhost:8080/actuator/health`  
To run all tests locally: `./mvnw clean test`

---

## Security Note

- **Admin Registration Boundary:** For security reasons, the `POST /api/auth/register` endpoint forces all new registrations to have the `USER` role. You **cannot** register an `ADMIN` account via REST API.
- **How to create an Admin:** Register a regular user first, then open your database tool (inside IntelliJ IDEA for example) and manually update the `role` column from `USER` to `ADMIN` for that specific record. This is a strict security boundary by design.
