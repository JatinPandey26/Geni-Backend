package com.geni.backend.Connector;


import com.geni.backend.common.CredentialField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ConnectorDefinition {
    ConnectorType type;
    String displayName;
    List<CredentialField> credentialFields;
}
