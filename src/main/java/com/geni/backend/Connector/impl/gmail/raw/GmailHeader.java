package com.geni.backend.Connector.impl.gmail.raw;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GmailHeader {
    private String name;
    private String value;
}
