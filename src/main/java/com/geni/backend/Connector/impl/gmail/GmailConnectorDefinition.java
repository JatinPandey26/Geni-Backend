package com.geni.backend.Connector.impl.gmail;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.ConnectorType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GmailConnectorDefinition extends ConnectorDefinition{

    public GmailConnectorDefinition() {
        super(
                ConnectorType.GMAIL,
                ConnectorType.GMAIL.getName(),
                List.of()
        );
    }
}

