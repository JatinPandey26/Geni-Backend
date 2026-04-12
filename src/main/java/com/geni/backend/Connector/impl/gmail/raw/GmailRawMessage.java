package com.geni.backend.Connector.impl.gmail.raw;

import com.geni.backend.common.TriggerPayload;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailRawMessage {

    private String id;
    private String threadId;
    private List<String> labelIds;
    private String snippet;

    private GmailMessageResponsePayload payload;

    private Long historyId;
    private Long internalDate;

    public String getHeader(String key) {
        if (payload == null || payload.getHeaders() == null) return null;

        return payload.getHeaders().stream()
                .filter(h -> key.equalsIgnoreCase(h.getName()))
                .map(GmailHeader::getValue)
                .findFirst()
                .orElse(null);
    }
}
