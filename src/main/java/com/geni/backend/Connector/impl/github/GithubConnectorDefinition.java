package com.geni.backend.Connector.impl.github;

import com.geni.backend.Connector.ConnectorDefinition;
import com.geni.backend.Connector.ConnectorType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GithubConnectorDefinition extends ConnectorDefinition {

    public GithubConnectorDefinition() {
        super(
                ConnectorType.GITHUB,
                ConnectorType.GITHUB.getName(),
                List.of()
        );
    }
}
