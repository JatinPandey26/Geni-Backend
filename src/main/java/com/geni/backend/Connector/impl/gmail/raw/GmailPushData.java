package com.geni.backend.Connector.impl.gmail.raw;

import com.geni.backend.common.TriggerPayload;
import lombok.Data;

@Data
public class GmailPushData implements TriggerPayload {
    private String emailAddress;
    private Long historyId;
}
