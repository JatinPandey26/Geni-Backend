package com.geni.backend.secret.service;

import com.geni.backend.secret.provider.SecretProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DefaultSecretService implements SecretService {

    private static final String SECRET_KEY_PREFIX = "secret/data/";
    private final SecretProvider provider;
    private final ObjectMapper objectMapper;

    @Override
    public <T> T getSecret(String key, Class<T> clazz) {
        Map<String, Object> map = provider.fetch(key);
        if (map == null) {
            return null;
        }
        return objectMapper.convertValue(map, clazz);
    }

    @Override
    public <T> String storeSecret(String key, T value) {
        key = SECRET_KEY_PREFIX+key;
        Map<String, Object> map =
                objectMapper.convertValue(value, Map.class);

        provider.save(key, map);
        return key;
    }

    @Override
    public void deleteSecret(String key) {
        provider.delete(key);
    }

    @Override
    public void update(String credentialRef, Map<String, Object> creds) {
        provider.update(credentialRef,creds);
    }
}