# Geni Backend Documentation

**Project:** Workflow Automation Engine  
**Version:** 1.0.0  
**Last Updated:** March 31, 2026  
**Status:** Active Development

---

## 🎯 What is Geni?

Geni is a **trigger-action workflow automation platform** that connects third-party services through a modular, scalable architecture. Build powerful automations without writing code—or extend the platform with custom connectors and actions.

**Example:** When a GitHub issue is created → automatically send an email via Gmail

---

## 📚 Quick Navigation

### For Different Users

#### 🚀 **I want to get started quickly**
- Start here: [5-Minute Quick Start](#quickstart)
- Then: [Create Your First Workflow](tutorials/YOUR_FIRST_WORKFLOW.md)
- Available integrations: [Connectors Overview](connectors/README.md)

#### 👨‍💻 **I'm a backend engineer**
- Start here: [System Architecture](ARCHITECTURE.md)
- Code structure: [Backend README](../README.md)
- Extend the platform:
    - [Add a new Trigger](tutorials/ADD_NEW_TRIGGER.md)
    - [Add a new Action](tutorials/ADD_NEW_ACTION.md)
    - [Add a new Connector](tutorials/ADD_NEW_CONNECTOR.md)
- Standards: [Contributing Guide](DEVELOPERS/CONTRIBUTING.md)

#### 🏗️ **I'm handling deployment/DevOps**
- Setup guide: [Installation](guides/INSTALLATION.md)
- Configure: [Configuration Guide](guides/CONFIGURATION.md)
- Deploy: [Docker Setup](deployment/DOCKER.md) | [Kubernetes](deployment/KUBERNETES.md)
- Security: [Vault Setup](guides/VAULT_SETUP.md) | [Authentication](guides/AUTHENTICATION.md)
- Monitor: [Monitoring & Debugging](guides/MONITORING.md)

#### 📊 **I'm product/business focused**
- Overview: [System Architecture](ARCHITECTURE.md) (high-level section)
- Available integrations: [All Connectors](connectors/README.md)
- Roadmap: [Future Improvements](WORKFLOW_AUTOMATION_GUIDE.md#future-improvements)
- Core concepts: [Architecture Overview](ARCHITECTURE.md#overview)

---

## 📖 Documentation by Topic

### Getting Started
- [Quick Start (5 minutes)](#quickstart)
- [Installation Guide](guides/INSTALLATION.md)
- [Configuration](guides/CONFIGURATION.md)
- [Your First Workflow](tutorials/YOUR_FIRST_WORKFLOW.md)

### Core Concepts
- [System Architecture](ARCHITECTURE.md)
- [Workflow Automation Guide](WORKFLOW_AUTOMATION_GUIDE.md) ⭐ **Core reference**
    - Components (Trigger, Action, Workflow, Connector, Integration)
    - Key concepts (TriggerEvent, ActionHandler, ExecutionContext)
    - Complete example walkthrough
    - Security design
    - Extensibility guide

### API Documentation
- [REST API Reference](API/REST_API.md)
- [Webhook Events](API/WEBHOOK_EVENTS.md)
- [Schema Reference](API/SCHEMA_REFERENCE.md)

### Integrations/Connectors
- [All Connectors Overview](connectors/README.md)
- [GitHub Connector](connectors/GITHUB.md)
- [Gmail Connector](connectors/GMAIL.md)
- [Slack Connector](connectors/SLACK.md)
- [Adding New Connectors](tutorials/ADD_NEW_CONNECTOR.md)

### Tutorials & Guides
- [Your First Workflow](tutorials/YOUR_FIRST_WORKFLOW.md)
- [Add Custom Trigger](tutorials/ADD_NEW_TRIGGER.md)
- [Add Custom Action](tutorials/ADD_NEW_ACTION.md)
- [Add Custom Connector](tutorials/ADD_NEW_CONNECTOR.md)
- [Common Patterns & Recipes](tutorials/COMMON_PATTERNS.md)

### Security & Authentication
- [Authentication Setup](guides/AUTHENTICATION.md)
- [Vault Configuration](guides/VAULT_SETUP.md)
- [Security Design](WORKFLOW_AUTOMATION_GUIDE.md#security-design)

### Deployment & Operations
- [Docker Deployment](deployment/DOCKER.md)
- [Kubernetes Deployment](deployment/KUBERNETES.md)
- [Production Checklist](deployment/PRODUCTION_CHECKLIST.md)
- [Upgrade Guide](deployment/UPGRADE_GUIDE.md)
- [Monitoring & Debugging](guides/MONITORING.md)

### Development
- [Contributing Guide](DEVELOPERS/CONTRIBUTING.md)
- [Code Style Standards](DEVELOPERS/CODE_STYLE.md)
- [Testing Guide](DEVELOPERS/TESTING.md)
- [Debugging Guide](DEVELOPERS/DEBUGGING.md)
- [Performance Optimization](DEVELOPERS/PERFORMANCE.md)
- [Architecture Decisions (ADR)](DEVELOPERS/ADR/README.md)

### Troubleshooting
- [Common Issues & FAQ](TROUBLESHOOTING/COMMON_ISSUES.md)
- [Error Code Reference](TROUBLESHOOTING/ERROR_CODES.md)
- [Support & Contact](TROUBLESHOOTING/SUPPORT.md)

---

## 🚀 Quick Start {#quickstart}

### Option 1: Local Setup (2 minutes)

```bash
# 1. Clone repository
git clone https://github.com/geni/backend.git
cd backend

# 2. Start database
docker-compose up -d

# 3. Run application
./mvnw spring-boot:run
