package com.geni.backend.Connector.impl.gmail.payload;

import com.geni.backend.common.TriggerPayload;
import lombok.Data;

@Data
public class GmailWebhookPayload implements TriggerPayload {
        private PubSubMessage message;
        private String subscription;

        @Data
        public static class PubSubMessage {
                private String data;
                private String messageId;
                private String publishTime;
        }
}
