package com.geni.backend.secret.service;

import java.util.Map;

public interface SecretService {
    <T> T getSecret(String key,Class<T> clazz);
    <T> String storeSecret(String key, T value);
    void deleteSecret(String key);
    void update(String credentialRef, Map<String, Object> accessToken);
}