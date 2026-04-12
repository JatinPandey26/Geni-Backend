package com.geni.backend.Connector.impl.gmail.payload;

import lombok.Data;

@Data
public class Attachment {
    private String fileName;
    private String mimeType;
    private String attachmentId;
    private int size;
}