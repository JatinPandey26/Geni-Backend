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
- [x] Workflow definition + storage
- [x] Execution engine
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
KeyVault             secrets management
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

Properties Setup (Take help from `.env.example`): [for now take help from admin for keys , we will be making a doc to generate keys for connectors in future]

```bash

**5. Run**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**6. Expose webhooks locally (GitHub needs a public URL)**
```bash
ngrok http 8080
# update Webhook URL in GitHub App settings to https://xxx.ngrok.io/webhooks/github
```
TODO: we will need to provide a doc for setting up connectors 

## Architecture decisions

See [`docs/adr/`](docs/adr/) for decisions and their rationale:
---

## Environment variables

take help from `.env.example` for env variables, we will be making a doc to generate keys for connectors in future

## Also check out:
- docs/Readme.md for detailed documentation [`docs/Readme.md`](docs/Readme.md)
