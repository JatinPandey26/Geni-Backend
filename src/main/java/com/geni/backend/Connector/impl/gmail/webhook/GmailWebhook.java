package com.geni.backend.Connector.impl.gmail.webhook;

import com.geni.backend.Connector.impl.gmail.Service.GmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public void handleRegistrationWebhook(
            @RequestParam Map<String,String> params){
        log.info("Received Gmail registration webhook with params: {}", params);
        this.gmailService.handleRegistration(params);
    }

    @PostMapping
    public ResponseEntity<Void> handleEventWebhook(
            @RequestParam Map<String,String> params, @RequestBody String rawBody){
        log.info("Received Gmail webhook event with params: {}", params);
        this.gmailService.handleEvent(params,rawBody);
        // this is imp else Gmail will retry mssg as missed ack
        return ResponseEntity.ok().build();
    }

}
