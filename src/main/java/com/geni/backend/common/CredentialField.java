package com.geni.backend.common;

import lombok.Builder;
import lombok.Value;

// CredentialField.java
// Describes one field shown in the UI when user sets up an integration
@Value
@Builder
public class CredentialField {
    String  key;
    String  label;
    boolean secret;
    boolean required;

    public static CredentialField secret(String key, String label) {
        return new CredentialField(key, label, true, true);
    }

    public static CredentialField plain(String key, String label) {
        return new CredentialField(key, label, false, true);
    }

    public static CredentialField optional(String key, String label) {
        return new CredentialField(key, label, false, false);
    }
}