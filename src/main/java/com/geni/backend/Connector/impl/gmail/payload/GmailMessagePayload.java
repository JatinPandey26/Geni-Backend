package com.geni.backend.Connector.impl.gmail.payload;

import com.geni.backend.common.TriggerPayload;
import lombok.Data;

import java.util.List;

@Data
public class GmailMessagePayload implements TriggerPayload {

    private String messageId;
    private String threadId;

    private String from;
    private String to;
    private String subject;

    private String body;     // plain text
    private String snippet;  // preview

    private Long internalDate;
    private List<Attachment> attachments;

}
