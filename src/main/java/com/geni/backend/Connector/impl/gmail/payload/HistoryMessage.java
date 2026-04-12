package com.geni.backend.Connector.impl.gmail.payload;

import com.geni.backend.Connector.impl.gmail.raw.GmailRawMessageRef;
import lombok.Data;

@Data
public class HistoryMessage {
    private GmailRawMessageRef message;
}
