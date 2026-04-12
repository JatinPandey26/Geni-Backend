package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GmailHistoryResponse {
    List<GmailHistory> history;
    String historyId;
}
