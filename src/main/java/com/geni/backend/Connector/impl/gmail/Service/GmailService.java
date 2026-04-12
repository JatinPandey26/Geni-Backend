package com.geni.backend.Connector.impl.gmail.Service;

import java.util.Map;

public interface GmailService {
    void handleRegistration(Map<String,String> payload);
    void handleEvent(Map<String,String> payload,String rawBody);
}
