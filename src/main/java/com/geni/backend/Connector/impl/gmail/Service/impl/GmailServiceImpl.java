package com.geni.backend.Connector.impl.gmail.Service.impl;

import com.geni.backend.Connector.ConnectorType;
import com.geni.backend.Connector.impl.gmail.Service.GmailService;
import com.geni.backend.integration.Service.IntegrationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class GmailServiceImpl implements GmailService {

    private final IntegrationService integrationService;

    @Override
    public void handleWebhook(Map<String, String> payload) {
        integrationService.createIntegration(ConnectorType.GMAIL.getType(), payload);
    }
}
