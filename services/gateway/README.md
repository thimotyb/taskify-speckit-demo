# Gateway

Single origin for the browser. Port **8080**. It is the one place where real authentication will be
added when the early-phase identification is replaced.

## Routes

| Path | Goes to |
|---|---|
| `/api/v1/projects/{id}/tasks`, `/api/v1/tasks/**` | task-service |
| `/api/v1/users/**`, `/api/v1/projects`, `/api/v1/projects/{id}` | project-service |
| `/internal/**` | nothing (404): internal endpoints are never exposed |

## Protections

- Security headers on every response (CSP, `nosniff`, `Referrer-Policy`, `no-store`)
- Strict CORS allow-list (`TASKIFY_ALLOWED_ORIGIN`, default `http://localhost:3000`)
- Request body limit (64 KB by default, 413 above it)
- Write rate limit per acting user: 60 POST/PUT/PATCH requests per minute, 429 above it

## Configuration

`TASKIFY_PROJECT_SERVICE_URL`, `TASKIFY_TASK_SERVICE_URL`, `TASKIFY_ALLOWED_ORIGIN`. No secrets.

## Run and test

```bash
mvn -pl services/gateway spring-boot:run     # from the repository root
mvn -pl services/gateway verify
```
