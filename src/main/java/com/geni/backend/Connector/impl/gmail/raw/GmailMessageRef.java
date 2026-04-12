package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GmailMessageRef {
    private String id;
    private String threadId;
}
