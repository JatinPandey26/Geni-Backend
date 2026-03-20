Geni
Lightweight, extensible workflow orchestration engine with deterministic execution, step-level observability, DAG support, and pluggable connector architecture.

What it does
Geni lets you define workflows that react to external events (GitHub issues, Jira tickets, webhooks) and execute a sequence of actions (send Slack message, create Jira ticket, post a comment). Workflows are JSON-defined, versioned, and executed with full step-level traceability.

Current status
V0.1 — in progress

 Connector system (Slack, GitHub, Jira)
 Integration management with pluggable auth (API Key, GitHub App, OAuth2)
 Webhook receiver (GitHub)
 Workflow definition + storage
 Execution engine
 Step-level logs


Tech stack
Java 21
Spring Boot 3.2
PostgreSQL 16        jsonb for flexible metadata storage
Spring Data JPA      with Specifications for dynamic queries
Spring Security      basic auth (V0.1)
JJWT                 GitHub App JWT signing
Docker Compose       local Postgres
GitHub Actions       CI



Local setup
1. Clone and configure
bashgit clone https://github.com/JatinPandey26/geni
cd geni
2. Start Postgres
bashdocker compose up -d
3. GitHub App private key
bash# convert GitHub's PKCS1 key to PKCS8
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt \
  -in ~/Downloads/geni-github-manager.pem \
  -out src/main/resources/github/private-key-pkcs8.pem
4. application-local.properties
propertiesspring.datasource.url=jdbc:postgresql://localhost:5432/geni
spring.datasource.username=geni
spring.datasource.password=geni

geni.connectors.github.app-id=3140015
geni.connectors.github.app-name=geni-github-manager
geni.connectors.github.webhook-secret=your-webhook-secret
geni.connectors.github.private-key-path=classpath:github/private-key-pkcs8.pem

geni.encryption.key=your-base64-32-byte-key
Generate an encryption key:
bashopenssl rand -base64 32
5. Run
bashmvn spring-boot:run -Dspring-boot.run.profiles=local
6. Expose webhooks locally (GitHub needs a public URL)
bashngrok http 8080
# update Webhook URL in GitHub App settings to https://xxx.ngrok.io/webhooks/github


Architecture decisions
See docs/adr/ for decisions and their rationale:
#Decision001String over enum for connectorType002Credentials separate from metadata003State token DB-backed over HMAC004Soft delete integrations

Environment variables
VariableDescriptionRequiredSPRING_DATASOURCE_URLPostgres JDBC URLYesSPRING_DATASOURCE_USERNAMEPostgres userYesSPRING_DATASOURCE_PASSWORDPostgres passwordYesGENI_ENCRYPTION_KEYBase64 32-byte AES keyYesGENI_CONNECTORS_GITHUB_APP_IDGitHub App IDFor GitHubGENI_CONNECTORS_GITHUB_WEBHOOK_SECRETGitHub webhook secretFor GitHub
