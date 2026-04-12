package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GmailMessageEvent {
    private String emailId;
    private String messageId;
    private String threadId;
    private List<String> labelIds;
    private Long historyId;
    private LocalDateTime receivedAt;
}