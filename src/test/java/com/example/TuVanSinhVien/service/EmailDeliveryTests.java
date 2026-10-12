package com.example.TuVanSinhVien.service;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailDeliveryTests {
    @Test void brevoUsesHttpsApiContractAndReportsRejections() throws Exception {
        var body = new AtomicReference<String>();
        var key = new AtomicReference<String>();
        var method = new AtomicReference<String>();
        var status = new java.util.concurrent.atomic.AtomicInteger(201);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v3/smtp/email", exchange -> {
            method.set(exchange.getRequestMethod());
            key.set(exchange.getRequestHeaders().getFirst("api-key"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        try {
            var client = new BrevoEmailClient("test-only-key", "sender@example.com", "QAUTE",
                    HttpClient.newHttpClient(), URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/v3/smtp/email"));
            assertTrue(client.send("student@example.com", "Mã xác thực", "<b>123456</b>"));
            assertEquals("POST", method.get());
            assertEquals("test-only-key", key.get());
            var json = JsonMapper.builder().build().readTree(body.get());
            assertEquals("sender@example.com", json.path("sender").path("email").asText());
            assertEquals("student@example.com", json.path("to").get(0).path("email").asText());
            assertEquals("Mã xác thực", json.path("subject").asText());
            assertEquals("<b>123456</b>", json.path("htmlContent").asText());
            for (int rejected : new int[]{401, 429, 500}) {
                status.set(rejected);
                assertFalse(client.send("student@example.com", "OTP", "123456"));
            }
        } finally { server.stop(0); }
    }

    @Test void otpAndNotificationsUseOnlySelectedTransport() throws Exception {
        var smtp = mock(JavaMailSender.class);
        var brevo = mock(BrevoEmailClient.class);
        when(brevo.send(anyString(), anyString(), anyString())).thenReturn(true);
        var service = new EmailService(smtp, brevo, "brevo", "sender@example.com");
        assertTrue(service.sendOtpEmail("student@example.com", "654321", 5));
        verify(brevo).send(eq("student@example.com"), eq("[QAUTE] Mã xác thực OTP"), contains("654321"));
        when(brevo.send(anyString(), anyString(), anyString())).thenReturn(false);
        assertFalse(service.sendNotificationEmail("student@example.com", "Thông báo", "Nội dung"));
        verifyNoInteractions(smtp);
        assertFalse(service.sendOtpEmail("", "654321", 5));
    }

    @Test void smtpRemainsAvailableLocallyAndProviderErrorsAreFailures() throws Exception {
        var smtp = mock(JavaMailSender.class);
        var brevo = mock(BrevoEmailClient.class);
        var message = new MimeMessage((Session) null);
        when(smtp.createMimeMessage()).thenReturn(message);
        var service = new EmailService(smtp, brevo, "smtp", "sender@example.com");
        assertTrue(service.sendOtpEmail("student@example.com", "123456", 5));
        assertEquals("[QAUTE] Mã xác thực OTP", message.getSubject());
        assertTrue(message.getFrom()[0].toString().contains("sender@example.com"));
        verify(smtp).send(message);
        verifyNoInteractions(brevo);
        when(brevo.send(anyString(), anyString(), anyString())).thenThrow(new java.io.IOException("test"));
        assertFalse(new EmailService(smtp, brevo, "brevo", "sender@example.com")
                .sendOtpEmail("student@example.com", "123456", 5));
        assertThrows(IllegalArgumentException.class, () -> new EmailService(smtp, brevo, "invalid", "sender@example.com"));
        assertFalse(new BrevoEmailClient("", "sender@example.com", "QAUTE")
                .send("student@example.com", "OTP", "123456"));
    }
}
