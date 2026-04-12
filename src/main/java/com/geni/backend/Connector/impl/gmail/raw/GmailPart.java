package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailPart {

    private String mimeType;
    private GmailRawBody body;
    private List<GmailPart> parts;
    private String filename;
}
