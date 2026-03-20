# Geni

Lightweight, extensible workflow orchestration engine with deterministic execution, step-level observability, DAG support, and pluggable connector architecture.

---

## What it does

Geni lets you define workflows that react to external events (GitHub issues, Jira tickets, webhooks) and execute a sequence of actions (send Slack message, create Jira ticket, post a comment). Workflows are JSON-defined, versioned, and executed with full step-level traceability.

---

## Current status

**V0.1 — in progress**

- [x] Connector system (Slack, GitHub, Jira)
- [x] Integration management with pluggable auth (API Key, GitHub App, OAuth2)
- [x] Webhook receiver (GitHub)
- [ ] Workflow definition + storage
- [ ] Execution engine
- [ ] Step-level logs

---

## Tech stack

```
Java 21
Spring Boot 3.2
PostgreSQL 16        jsonb for flexible metadata storage
Spring Data JPA      with Specifications for dynamic queries
Spring Security      basic auth (V0.1)
JJWT                 GitHub App JWT signing
Docker Compose       local Postgres
GitHub Actions       CI
```


## Local setup

**1. Clone and configure**
```bash
git clone https://github.com/JatinPandey26/geni
cd geni
```

**2. Start Postgres**
```bash
docker compose up -d
```

**3. GitHub App private key**
```bash
# convert GitHub's PKCS1 key to PKCS8
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt \
  -in ~/Downloads/geni-github-manager.pem \
  -out src/main/resources/github/private-key-pkcs8.pem
```

**4. `application-local.properties`**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geni
spring.datasource.username=geni
spring.datasource.password=geni

geni.connectors.github.app-id=3140015
geni.connectors.github.app-name=geni-github-manager
geni.connectors.github.webhook-secret=your-webhook-secret
geni.connectors.github.private-key-path=classpath:github/private-key-pkcs8.pem

geni.encryption.key=your-base64-32-byte-key
```

Generate an encryption key:
```bash
openssl rand -base64 32
```

**5. Run**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**6. Expose webhooks locally (GitHub needs a public URL)**
```bash
ngrok http 8080
# update Webhook URL in GitHub App settings to https://xxx.ngrok.io/webhooks/github
```

## Architecture decisions

See [`docs/adr/`](docs/adr/) for decisions and their rationale:
---

## Environment variables

| Variable | Description | Required |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Postgres JDBC URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | Postgres user | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Postgres password | Yes |
| `GENI_ENCRYPTION_KEY` | Base64 32-byte AES key | Yes |
| `GENI_CONNECTORS_GITHUB_APP_ID` | GitHub App ID | For GitHub |
| `GENI_CONNECTORS_GITHUB_WEBHOOK_SECRET` | GitHub webhook secret | For GitHub |
