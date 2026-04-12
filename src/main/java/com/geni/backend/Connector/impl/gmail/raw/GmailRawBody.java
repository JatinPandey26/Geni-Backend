package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GmailRawBody {
    private String data; // base64 encoded
    private Integer size;
    private String attachmentId;
}
