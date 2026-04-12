package com.geni.backend.Connector.impl.gmail.payload;

import com.geni.backend.Connector.impl.gmail.raw.GmailMessageResponsePayload;
import com.geni.backend.Connector.impl.gmail.raw.GmailPart;
import com.geni.backend.Connector.impl.gmail.raw.GmailRawMessage;

import java.util.Base64;

public class GmailMessageHelper {

    public String extractBody(GmailRawMessage message) {
        GmailMessageResponsePayload payload = message.getPayload();

        if (payload.getParts() != null) {

            for (GmailPart part : payload.getParts()) {

                if ("text/plain".equals(part.getMimeType())) {
                    return decode(part.getBody().getData());
                }
            }

            // fallback to html
            for (GmailPart part : payload.getParts()) {
                if ("text/html".equals(part.getMimeType())) {
                    return decode(part.getBody().getData());
                }
            }
        }

        return null;
    }

    private String decode(String data) {
        return new String(Base64.getDecoder().decode(data));
    }
}
