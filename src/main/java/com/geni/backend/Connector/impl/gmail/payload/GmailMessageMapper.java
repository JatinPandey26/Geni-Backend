package com.geni.backend.Connector.impl.gmail.payload;

import com.geni.backend.Connector.impl.gmail.client.GmailConnectorClient;
import com.geni.backend.Connector.impl.gmail.raw.GmailHeader;
import com.geni.backend.Connector.impl.gmail.raw.GmailPart;
import com.geni.backend.Connector.impl.gmail.raw.GmailRawBody;
import com.geni.backend.Connector.impl.gmail.raw.GmailRawMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GmailMessageMapper {

    public static GmailMessagePayload map(GmailRawMessage msg) {

        GmailMessagePayload email = new GmailMessagePayload();

        email.setMessageId(msg.getId());
        email.setThreadId(msg.getThreadId());
        email.setSnippet(msg.getSnippet());
        email.setInternalDate(msg.getInternalDate());

        // headers
        Map<String, String> headers = toHeaderMap(msg);

        email.setFrom(extractEmail(headers.get("from")));
        email.setTo(extractEmail(headers.get("to")));
        email.setSubject(headers.get("subject"));

        // ✅ body extraction (nested parts)
        email.setBody(extractBody(msg));

        // ✅ attachments
        email.setAttachments(extractAttachments(msg));

        return email;
    }


    // ==============================
    // HEADERS
    // ==============================

    private static Map<String, String> toHeaderMap(GmailRawMessage msg) {
        return msg.getPayload().getHeaders().stream()
                .collect(Collectors.toMap(
                        h -> h.getName().toLowerCase(),
                        GmailHeader::getValue,
                        (v1, v2) -> v1
                ));
    }

    private static String extractEmail(String value) {
        if (value == null) return null;

        Matcher m = Pattern.compile("<(.+?)>").matcher(value);
        return m.find() ? m.group(1) : value;
    }

    // ==============================
    // BODY (PARTS ONLY)
    // ==============================

    private static String extractBody(GmailRawMessage msg) {

        if (msg.getPayload() == null || msg.getPayload().getParts() == null) {
            return null;
        }

        // ✅ Prefer HTML
        String html = extractFromParts(msg.getPayload().getParts(), "text/html");
        if (html != null) return html;

        // ✅ fallback to plain text
        return extractFromParts(msg.getPayload().getParts(), "text/plain");
    }

    private static String extractFromParts(List<GmailPart> parts, String mimeType) {
        if (parts == null) return null;

        for (GmailPart part : parts) {

            // ✅ match
            if (mimeType.equalsIgnoreCase(part.getMimeType())
                    && part.getBody() != null
                    && part.getBody().getData() != null) {
                return decode(part.getBody().getData());
            }

            // ✅ recurse
            String result = extractFromParts(part.getParts(), mimeType);
            if (result != null) return result;
        }

        return null;
    }

    // ==============================
    // ATTACHMENTS
    // ==============================

    private static List<Attachment> extractAttachments(GmailRawMessage msg) {
        List<Attachment> attachments = new ArrayList<>();

        if (msg.getPayload() != null && msg.getPayload().getParts() != null) {
            collectAttachments(msg.getPayload().getParts(), attachments);
        }

        return attachments;
    }

    private static void collectAttachments(List<GmailPart> parts, List<Attachment> attachments) {
        if (parts == null) return;

        for (GmailPart part : parts) {

            GmailRawBody body = part.getBody();

            // ✅ attachment detection
            if (part.getFilename() != null && !part.getFilename().isEmpty()
                    && body != null
                    && body.getAttachmentId() != null) {

                Attachment att = new Attachment();
                att.setFileName(part.getFilename());
                att.setMimeType(part.getMimeType());
                att.setAttachmentId(body.getAttachmentId());
                att.setSize(body.getSize());

                attachments.add(att);
            }

            // ✅ recurse
            collectAttachments(part.getParts(), attachments);
        }
    }

    // ==============================
    // BASE64 DECODE (SAFE)
    // ==============================

    private static String decode(String b64Enc) {
        if (b64Enc == null) return null;

        try {
            return new String(Base64.getUrlDecoder().decode(b64Enc));
        } catch (IllegalArgumentException e) {
            return new String(Base64.getDecoder().decode(b64Enc));
        }
    }
}