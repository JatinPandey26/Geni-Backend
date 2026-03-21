package com.geni.backend.Connector.impl.github;

import com.geni.backend.Connector.impl.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/github/webhook")
@Slf4j
@RequiredArgsConstructor
public class GithubWebhook {

    private final GithubService githubService;

    @PostMapping
    public void handleEventWebhook(@RequestHeader("X-GitHub-Event")      String eventHeader,
                                   @RequestHeader("X-Hub-Signature-256") String signature,
                                   @RequestHeader("X-GitHub-Delivery")   String deliveryId,
                                   @RequestBody String rawBody){
        log.info("github webhook came");
        Map<String, String> headers = Map.of(
                "X-GitHub-Event",      eventHeader,
                "X-Hub-Signature-256", signature,
                "X-GitHub-Delivery",   deliveryId
        );

        this.githubService.handleEvent(headers,rawBody);

    }




}
