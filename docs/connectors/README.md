# Connectors Overview

**Last Updated:** March 31, 2026  
**Status:** Active

---

## 🎯 What are Connectors?

Connectors are the **integration points** that allow Geni to communicate with external services. Each connector provides:

- **Authentication** - OAuth 2.0 setup and token management
- **Triggers** - Events that can start workflows (e.g., "GitHub Issue Created")
- **Actions** - Operations that can be performed (e.g., "Send Gmail Email")
- **Client Library** - Authenticated API calls to the service

**Example:** The Gmail connector lets you send emails as actions in your workflows.

---

## 📊 Available Connectors

| Connector | Status | Triggers | Actions | Authentication |
|-----------|--------|----------|---------|----------------|
| **GitHub** | ✅ **Active** | Issue created, PR opened, Release published, Push events | Create issue, Add comment, Create PR, Update issue | OAuth App |
| **Gmail** | ✅ **Active** | — | Send email, Draft email, Get emails | OAuth 2.0 |
| **Slack** | 🔄 **Planned** | — | Send message, Send DM, Create channel | OAuth 2.0 |
| **Atlassian** | 🔄 **Planned** | Issue created, Ticket updated, Sprint started | Create issue, Update ticket, Add comment | OAuth 2.0 |

---

## 🔌 Active Connectors

### GitHub Connector
**Status:** ✅ Production Ready

**What it does:**
- Monitors GitHub repositories for events
- Performs repository operations via GitHub API

**Common Use Cases:**
- Notify team when issues are created
- Auto-assign reviewers to pull requests
- Create release notes from merged PRs

**Triggers:**
- `github:issue.opened` - New issue created
- `github:pull_request.opened` - New PR opened
- `github:release.published` - Release published
- `github:push` - Code pushed to repository

**Actions:**
- `github:create_issue` - Create new issue
- `github:add_comment` - Add comment to issue/PR
- `github:create_pull_request` - Create new PR

**[Full Documentation](GITHUB.md)** | **[Add to Workflow](../tutorials/YOUR_FIRST_WORKFLOW.md)**

---

### Gmail Connector
**Status:** ✅ Production Ready

**What it does:**
- Send emails via Gmail API
- Draft and manage emails programmatically

**Common Use Cases:**
- Send notifications when workflows complete
- Auto-respond to support tickets
- Forward important alerts to team members

**Triggers:**
- None (Gmail doesn't provide webhook triggers)

**Actions:**
- `gmail:send_email` - Send email immediately
- `gmail:draft_email` - Create email draft
- `gmail:get_emails` - Retrieve emails from inbox

**[Full Documentation](GMAIL.md)** | **[Add to Workflow](../tutorials/YOUR_FIRST_WORKFLOW.md)**

---

## 🔄 Planned Connectors

### Slack Connector
**Status:** 🔄 In Development (Q2 2026)

**Planned Features:**
- Send messages to channels and users
- Create channels and manage membership
- React to messages and threads

**Use Cases:**
- Notify teams about deployments
- Send alerts to specific channels
- Create incident response channels

---

### Atlassian Connector (Jira/Confluence)
**Status:** 🔄 Planned (Q3 2026)

**Planned Features:**
- Monitor Jira tickets and sprints
- Create/update Confluence pages
- Sync issues between systems

**Use Cases:**
- Create Jira tickets from GitHub issues
- Update Confluence docs when code changes
- Sync sprint progress across tools

---

## 🔧 How Connectors Work

### 1. Authentication Setup
Each connector requires OAuth 2.0 setup:
- Register application with the service
- Configure redirect URIs
- Store client credentials securely

**Security:** All tokens are stored in HashiCorp Vault, never in the database.

### 2. Webhook Registration
For trigger-enabled connectors:
- Geni registers webhooks with the external service
- Webhooks deliver events to `/api/v1/webhooks/{connector}`
- Events are validated and converted to `TriggerEvent` objects

### 3. Action Execution
When workflows run:
- Load integration credentials from Vault
- Create authenticated client
- Execute action with provided parameters
- Return results to workflow engine

### 4. Error Handling
- Automatic token refresh on expiration
- Retry logic for transient failures
- Detailed error logging and user feedback

---

## 🚀 Using Connectors in Workflows

### Basic Workflow Structure
```json
{
  "name": "GitHub to Gmail",
  "trigger": {
    "type": "github:issue.opened",
    "filters": {
      "repository": "myorg/backend"
    }
  },
  "actions": [
    {
      "type": "gmail:send_email",
      "integration_id": "gmail-integration-123",
      "inputs": {
        "to": "team@company.com",
        "subject": "New Issue: ${trigger.title}",
        "body": "${trigger.body}"
      }
    }
  ]
}
```

### Integration Setup
Before using connectors:
1. Connect your account via OAuth
2. Integration is stored with encrypted credentials
3. Reference integration in workflow actions

**Setup Guide:** [Authentication Guide](../guides/AUTHENTICATION.md)

---

## 🛠️ Adding New Connectors

### When to Add a Connector
- Need integration with a new service
- Service has REST API and OAuth support
- Want to trigger on service events
- Need to perform actions in the service

### Development Process
1. **Create Connector Definition** - Metadata and configuration
2. **Implement Client** - Authenticated API calls
3. **Add Actions** - Define what users can do
4. **Add Triggers** - Define events to monitor
5. **Test Integration** - End-to-end testing
6. **Document** - Create connector documentation

**Step-by-Step Guide:** [Add New Connector](../tutorials/ADD_NEW_CONNECTOR.md)

### Template
Use our [Connector Template](TEMPLATE.md) to get started quickly.

---

## 📋 Connector Requirements

### Must Have
- ✅ OAuth 2.0 authentication
- ✅ REST API access
- ✅ Proper error handling
- ✅ Token refresh capability
- ✅ Comprehensive documentation

### Should Have
- 🔄 Webhook support for triggers
- 🔄 Rate limiting awareness
- 🔄 Pagination for large datasets
- 🔄 Comprehensive test coverage

### Nice to Have
- 🎯 Real-time event streaming
- 🎯 Bulk operations
- 🎯 Advanced filtering options
- 🎯 Service-specific optimizations

---

## 🔍 Troubleshooting Connectors

### Common Issues

**"Integration not found"**
- Check integration ID in workflow
- Verify integration was created successfully
- Ensure user has access to the integration

**"Token expired"**
- Automatic refresh should handle this
- Check Vault connectivity
- Verify OAuth app configuration

**"Rate limit exceeded"**
- Implement exponential backoff
- Check service rate limits
- Consider premium API tiers

**"Webhook not receiving events"**
- Verify webhook URL is accessible
- Check webhook secret configuration
- Confirm service webhook settings

**Full Troubleshooting:** [Common Issues](../TROUBLESHOOTING/COMMON_ISSUES.md)

---

## 📈 Connector Metrics

We track connector health and usage:

- **Success Rate** - % of actions that complete successfully
- **Response Time** - Average time for API calls
- **Error Rate** - % of failed operations
- **Token Refresh Rate** - How often tokens need refreshing
- **Usage Volume** - Actions executed per day/week

**Monitor:** [Monitoring Guide](../guides/MONITORING.md)

---

## 🎯 Next Steps

### For Users
- [Create Your First Workflow](../tutorials/YOUR_FIRST_WORKFLOW.md)
- [Connect GitHub Account](../guides/AUTHENTICATION.md)
- [Explore Example Workflows](../tutorials/COMMON_PATTERNS.md)

### For Developers
- [Add New Connector](../tutorials/ADD_NEW_CONNECTOR.md)
- [Contribute to Existing Connectors](../DEVELOPERS/CONTRIBUTING.md)
- [Review Connector Architecture](../ARCHITECTURE.md#connectors)

### For DevOps
- [Configure OAuth Apps](../guides/AUTHENTICATION.md)
- [Set Up Vault](../guides/VAULT_SETUP.md)
- [Monitor Connector Health](../guides/MONITORING.md)

---

## 📞 Support

**Need Help with Connectors?**
- 📧 Email: support@geni.io
- 🐛 Issues: [GitHub Issues](https://github.com/geni/backend/issues)
- 💬 Discussions: [GitHub Discussions](https://github.com/geni/backend/discussions)

**Connector-Specific Issues:**
- GitHub: Check [GitHub Docs](GITHUB.md)
- Gmail: Check [Gmail Docs](GMAIL.md)

---

## 🔗 Related Documentation

- [System Architecture](../ARCHITECTURE.md)
- [Workflow Automation Guide](../WORKFLOW_AUTOMATION_GUIDE.md)
- [Authentication Setup](../guides/AUTHENTICATION.md)
- [Security Design](../WORKFLOW_AUTOMATION_GUIDE.md#security-design)
- [API Reference](../API/REST_API.md)

---

**Last Updated:** March 31, 2026  
**Maintained by:** Geni Team  
**Status:** Active 