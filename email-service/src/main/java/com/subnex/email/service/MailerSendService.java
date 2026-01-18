package com.subnex.email.service;

import com.subnex.email.dto.LoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailerSendService {

    private final WebClient webClient = WebClient.create("https://api.mailersend.com");

    @Value("${mailersend.api-key}")
    private String apiKey;

    @Value("${mailersend.from-email}")
    private String fromEmail;

    @Value("${mailersend.from-name:Subnex Auth Service}")
    private String fromName;

    public void sendLoginNotification(LoginEvent event) {
        log.info("Sending login notification to: {}", event.getUserEmail());

        String htmlContent = buildHtmlContent(event);
        String textContent = buildTextContent(event);

        Map<String, Object> body = Map.of(
            "from", Map.of("email", fromEmail, "name", fromName),
            "to", new Object[]{
                Map.of("email", event.getUserEmail(), "name", "User")
            },
            "subject", "Login Notification - " + event.getLoginTime(),
            "html", htmlContent,
            "text", textContent
        );

        webClient.post()
            .uri("/v1/email")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> log.info("Email sent successfully to {}", event.getUserEmail()))
            .doOnError(error -> log.error("Failed to send email to {}: {}", event.getUserEmail(), error.getMessage()))
            .subscribe();
    }

    private String buildHtmlContent(LoginEvent event) {
        return String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<h2>Login Notification</h2>" +
            "<p>Hello,</p>" +
            "<p>We detected a login to your account.</p>" +
            "<h3>Login Details:</h3>" +
            "<ul>" +
            "<li><strong>Email:</strong> %s</li>" +
            "<li><strong>Time:</strong> %s</li>" +
            "<li><strong>IP Address:</strong> %s</li>" +
            "<li><strong>Device:</strong> %s</li>" +
            "</ul>" +
            "<p>If this wasn't you, please secure your account immediately.</p>" +
            "<p>Best regards,<br>Subnex Team</p>" +
            "</body>" +
            "</html>",
            event.getUserEmail(),
            event.getLoginTime(),
            event.getIpAddress(),
            event.getDeviceInfo()
        );
    }

    private String buildTextContent(LoginEvent event) {
        return String.format(
            "Hello,\n\n" +
            "We detected a login to your account.\n\n" +
            "Login Details:\n" +
            "- Email: %s\n" +
            "- Time: %s\n" +
            "- IP Address: %s\n" +
            "- Device: %s\n\n" +
            "If this wasn't you, please secure your account immediately.\n\n" +
            "Best regards,\n" +
            "Subnex Team",
            event.getUserEmail(),
            event.getLoginTime(),
            event.getIpAddress(),
            event.getDeviceInfo()
        );
    }
}
