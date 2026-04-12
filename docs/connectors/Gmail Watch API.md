# Gmail Push Notifications via Pub/Sub (Watch API)

This doc explains how to set up Gmail push notifications using Pub/Sub + Gmail Watch API.

The goal is simple: whenever a new email comes in, we get notified and then fetch the actual email ourselves.

---

## Overview

Gmail does not send email data directly.

Instead, it only sends a signal that “something changed”.

Flow looks like:

1. Gmail publishes an event to Pub/Sub
2. Pub/Sub pushes that event to our webhook
3. We call Gmail APIs to fetch actual email data

---

## Architecture

```
Gmail → Pub/Sub Topic → Push Subscription → Webhook → Gmail API (history/messages)
```

---

## Prerequisites

- Google Cloud project
- Gmail API enabled
- OAuth token with scope:
  ```
  https://www.googleapis.com/auth/gmail.readonly
  ```
- Public HTTPS webhook (must be reachable from internet)

---

## 1. Create Pub/Sub Topic

- Go to Google Cloud Console
- Pub/Sub → Topics → Create topic

Example:
```
projects/<project-id>/topics/gmail-watch-topic
```

Keep this exact value, you'll need it in the watch request.

---

## 2. Create Push Subscription

- Open the topic → Create subscription
- Type: Push
- Endpoint:
```
https://your-domain.com/api/gmail/webhook
```

This is where Pub/Sub will send events.

---

## 3. Give Gmail Permission (Important)

This is the most common reason things don’t work.

Add this principal to the topic:
```
gmail-api-push@system.gserviceaccount.com
```

Role:
```
Pub/Sub Publisher
```

If this is missing, Gmail won’t be able to publish events.

---

## 4. Enable Gmail API

- Go to APIs & Services → Library
- Enable Gmail API

---

## 5. Start Watch

### Endpoint
```
POST https://gmail.googleapis.com/gmail/v1/users/me/watch
```

### Headers
```
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Body
```json
{
  "topicName": "projects/<project-id>/topics/gmail-watch-topic",
  "labelIds": ["INBOX"]
}
```

### Response
```json
{
  "historyId": "123456",
  "expiration": "1710000000000"
}
```

- `historyId` → starting point for fetching changes
- `expiration` → when watch will stop working

Store both.

---

## 6. Renew Watch

Watch expires in ~7 days.

If you don’t renew, everything silently stops.

### What to do:

- Store `expiration`
- Run a job (cron)
- Call watch API again before expiry

---

## Best Practices

### Idempotency
- Store processed messageId/historyId
- Avoid duplicate workflow execution

---

### Retry Handling
- Pub/Sub retries automatically
- Always return `200 OK` after success

---

### Logging
Log at least:
- historyId
- messageId
- raw webhook payload (for debugging)

---

### Security
- Validate incoming requests (if needed)
- Keep webhook protected

---

**Maintained by:** Geni Team  
**Status:** Active