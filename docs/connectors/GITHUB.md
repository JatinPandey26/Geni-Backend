# 🐙 GitHub Connector

**Status:** ✅ Production Ready  
**Connector Key:** `GITHUB`

---

## 🎯 Overview

The GitHub connector enables Geni to:

- Listen to repository events via webhooks
- Perform actions using GitHub APIs
- Trigger and automate workflows based on GitHub activity

---

## 🔐 OAuth Setup

To connect GitHub, create an OAuth App in GitHub:

1. Go to **Settings → Developer Settings → OAuth Apps**
2. Click **New OAuth App**
3. Configure:

| Field                      | Value                                               |
|----------------------------|-----------------------------------------------------|
| Application Name           | Geni                                                |
| Homepage URL               | `http://localhost:3000`                             |
| Authorization Callback URL | `http://localhost:8080/api/v1/auth/github/callback` |
| Webhook Endpoint           | `https://localhost:8080/v1/github/webhook`          |
Use grok for local setup :
See [Grok Setup](grok.md)

4. Copy:
    - Client ID
    - Client Secret
    - App ID


5. Create a webhook secret
   - copy it.

---

## ⚙️ Application Configuration

Paste these config properties in your .env (you can use .env.example to create your own dotenv file).
```dotenv
GITHUB_APP_ID=
GITHUB_APP_NAME=
GITHUB_CLIENT_ID=
GITHUB_WEBHOOK_SECRET=
GITHUB_PRIVATE_KEY_PATH=classpath:github/your-private-key.pem
```

## Installation Flow
1. Geni generates a **JWT (JSON Web Token)** using the GitHub App private key
2. JWT is sent to GitHub to request an **installation access token**
3. GitHub returns a **short-lived access token (~1 hour)**
4. This token is used to call GitHub APIs
5. If the token expires, it is automatically refreshed
Refer [Github Client](../../src/main/java/com/geni/backend/Connector/impl/github/client/GithubConnectorClient.java)

## Vault 

Vault Key - GITHUB_[#Installation_id]

In vault, we store [this token we use to do operation in github].
```
{
  "expires_at": "2026-04-09T08:05:41Z",
  "permissions": {
    "contents": "read",
    "discussions": "write",
    "issues": "write",
    "metadata": "read",
    "pull_requests": "write",
    "repository_projects": "read",
    "statuses": "read"
  },
  "repository_selection": "all",
  "token": "TOKEN_HERE"
}
```

## 🔔 Triggers

| Key | Description |
|-----|------------|
| `GITHUB_ISSUE_OPENED` | Triggered when a new issue is created |
| `GITHUB_ISSUE_CLOSED` | Triggered when an issue is closed |
| `GITHUB_ISSUE_UPDATED` | Triggered when an issue is updated |
| `GITHUB_ISSUE_REOPENED` | Triggered when a closed issue is reopened |
| `GITHUB_ISSUE_ASSIGNED` | Triggered when an issue is assigned to a user |
| `GITHUB_PR_OPENED` | Triggered when a pull request is opened |
| `GITHUB_PR_MERGED` | Triggered when a pull request is merged |
| `GITHUB_REVIEW_REQUESTED` | Triggered when a review is requested on a pull request |
| `GITHUB_REVIEW_SUBMITTED` | Triggered when a pull request review is submitted |
| `GITHUB_PR_COMMENT_ADDED` | Triggered when a comment is added to a pull request |
| `GITHUB_PUSH` | Triggered when code is pushed to a repository |

---

## 🎬 Actions

| Key | Description |
|-----|------------|
| `GITHUB_CREATE_ISSUE` | Creates a new GitHub issue |
| `GITHUB_ISSUE_COMMENT_CREATE` | Adds a comment to a GitHub issue |