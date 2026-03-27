package com.geni.backend.Connector.impl.gmail.Service;

import java.util.Map;

public interface GmailService {
    void handleWebhook(Map<String,String> payload);
}
