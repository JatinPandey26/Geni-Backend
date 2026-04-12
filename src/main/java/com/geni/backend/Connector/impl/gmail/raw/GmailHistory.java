package com.geni.backend.Connector.impl.gmail.raw;

import com.geni.backend.Connector.impl.gmail.payload.HistoryMessage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailHistory {
    private String id;
    private List<HistoryMessage> messagesDeleted;
    private List<GmailMessageRef> messages;
    private List<GmailMessageAdded> messagesAdded;
}
