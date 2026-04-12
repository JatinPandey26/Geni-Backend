package com.geni.backend.Connector.impl.gmail.raw;

import com.geni.backend.common.TriggerPayload;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GmailMessageAdded implements TriggerPayload {
    private GmailRawMessageRef message;
}
