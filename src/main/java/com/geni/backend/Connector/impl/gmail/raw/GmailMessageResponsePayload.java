package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailMessageResponsePayload {
    private String mimeType;
    private List<GmailHeader> headers;
    private List<GmailPart> parts;
}
