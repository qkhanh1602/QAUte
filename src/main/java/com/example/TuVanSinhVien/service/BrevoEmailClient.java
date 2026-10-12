package com.example.TuVanSinhVien.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class BrevoEmailClient {
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final HttpClient client;
    private final URI endpoint;

    @Autowired
    public BrevoEmailClient(@Value("${app.email.brevo-api-key:}") String apiKey,
            @Value("${app.email.from:qaute.edu@gmail.com}") String senderEmail,
            @Value("${app.email.from-name:QAUTE - Ban Tư Vấn Sinh Viên}") String senderName) {
        this(apiKey, senderEmail, senderName,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5))
                        .followRedirects(HttpClient.Redirect.NEVER).build(),
                URI.create("https://api.brevo.com/v3/smtp/email"));
    }

    BrevoEmailClient(String apiKey, String senderEmail, String senderName, HttpClient client, URI endpoint) {
        this.apiKey = apiKey; this.senderEmail = senderEmail; this.senderName = senderName;
        this.client = client; this.endpoint = endpoint;
    }

    public boolean send(String recipient, String subject, String html) throws Exception {
        if (apiKey.isBlank() || senderEmail.isBlank() || recipient == null || recipient.isBlank()) return false;
        String body = JsonMapper.builder().build().writeValueAsString(Map.of(
                "sender", Map.of("email", senderEmail, "name", senderName),
                "to", List.of(Map.of("email", recipient)), "subject", subject, "htmlContent", html));
        var request = HttpRequest.newBuilder(endpoint).timeout(Duration.ofSeconds(10))
                .header("api-key", apiKey).header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        // Do not log provider responses: they can contain recipient data or email content.
        var response = client.send(request, HttpResponse.BodyHandlers.discarding());
        return response.statusCode() == 201;
    }
}
