package com.geni.backend.Connector.impl.gmail;

import com.geni.backend.Connector.impl.gmail.Service.GmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/gmail/webhook")
@Slf4j
@RequiredArgsConstructor
public class GmailWebhook {

    private final GmailService gmailService;

    @GetMapping
    public void handleEventWebhook(
            @RequestParam Map<String,String> params){
        log.info("Received Gmail webhook with params: {}", params);
        this.gmailService.handleWebhook(params);
    }

}
