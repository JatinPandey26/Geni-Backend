package com.geni.backend.Connector.client;

import com.geni.backend.Connector.ConnectorType;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectorClientRegistry {

    Map<ConnectorType, ConnectorClient> clientMap;

    public ConnectorClientRegistry(List<ConnectorClient> clients){
        this.clientMap = new ConcurrentHashMap<>();
        log.info("Initializing ConnectorClientRegistry with clients: {}", clients);
        clients.stream().forEach(c -> {
            if(clientMap.get(c.getConnectorType()) != null){
                throw new IllegalStateException("Duplicate ConnectorClient for type: " + c.getConnectorType());
            }
            clientMap.put(c.getConnectorType(), c);
        });
    }

    public ConnectorClient find(ConnectorType type){
        return clientMap.get(type);
    }

    public ConnectorClient findOrThrow(ConnectorType type){
        ConnectorClient client = find(type);
        if(client == null){
            throw new IllegalArgumentException("Unknown ConnectorClient type: " + type);
        }
        return client;
    }

}
