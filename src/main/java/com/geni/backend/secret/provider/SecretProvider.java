package com.geni.backend.secret.provider;

import java.util.Map;

public interface SecretProvider {
    Map<String,Object> fetch(String key);
    void save(String key, Map<String,Object> value);
    void delete(String key);
}
