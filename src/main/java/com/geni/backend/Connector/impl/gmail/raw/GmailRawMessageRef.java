package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailRawMessageRef {
    private String id;
    private String threadId;
    private List<String> labelIds;
}
