package com.geni.backend.secret.provider;

import com.geni.backend.secret.properties.VaultProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VaultSecretProvider implements SecretProvider{

    private final VaultProperties vaultProperties;
    private final RestTemplate restTemplate;

    @Override
    public Map<String,Object> fetch(String key) {
        String url = vaultProperties.getUri() + "/v1/" + key;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultProperties.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        return (Map<String, Object>) ((Map) response.getBody().get("data")).get("data");
    }

    @Override
    public void save(String key, Map<String, Object> value) {
        String url = vaultProperties.getUri() + "/v1/" + key;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultProperties.getToken());

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(Map.of("data", value), headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    @Override
    public void delete(String key) {
        String url = vaultProperties.getUri() + "/v1/" + key;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Vault-Token", vaultProperties.getToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
