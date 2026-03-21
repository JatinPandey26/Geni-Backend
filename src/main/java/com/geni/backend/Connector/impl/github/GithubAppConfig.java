package com.geni.backend.Connector.impl.github;

import com.geni.backend.common.exception.WebhookSignatureException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

@Component
@ConfigurationProperties(prefix = "geni.connectors.github")
@Data
public class GithubAppConfig {

    private String appId;
    private String appName;
    private String clientId;
    private String webhookSecret;
    private String privateKeyPath;

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        this.privateKey = loadPrivateKey();
    }

    // ── JWT ──────────────────────────────────────────────────────────

    public String buildJwt() {
        Instant now = Instant.now();
        return Jwts.builder()
                .claim("iss", appId)
                .issuedAt(Date.from(now.minusSeconds(60)))    // 60s clock skew buffer
                .expiration(Date.from(now.plusSeconds(540)))  // 9 min — max is 10
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    // ── Webhook signature verification ───────────────────────────────

    public void verifyWebhookSignature(String rawBody, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new WebhookSignatureException("Missing X-Hub-Signature-256 header");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"
            ));
            byte[] hash     = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);

            // constant time comparison — prevents timing attacks
            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signatureHeader.getBytes(StandardCharsets.UTF_8)
            )) {
                throw new WebhookSignatureException("Signature mismatch");
            }
        } catch (WebhookSignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new WebhookSignatureException("Verification failed: " + e.getMessage());
        }
    }

    // ── Private key loader ───────────────────────────────────────────

    private PrivateKey loadPrivateKey() {
        try {
            Resource resource = new DefaultResourceLoader().getResource(privateKeyPath);

            String pem = new String(resource.getInputStream().readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(pem);

            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(keyBytes));

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load GitHub private key from: " + privateKeyPath, e
            );
        }
    }
}
