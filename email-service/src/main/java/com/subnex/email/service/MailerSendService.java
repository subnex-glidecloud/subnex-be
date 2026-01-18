package com.subnex.email.service;

import com.subnex.email.dto.LoginEvent;
import com.subnex.email.dto.PaymentEvent;
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

        String htmlContent = buildLoginHtmlContent(event);
        String textContent = buildLoginTextContent(event);

        Map<String, Object> body = Map.of(
            "from", Map.of("email", fromEmail, "name", fromName),
            "to", new Object[]{
                Map.of("email", event.getUserEmail(), "name", "User")
            },
            "subject", "Login Notification - " + event.getLoginTime(),
            "html", htmlContent,
            "text", textContent
        );

        sendEmail(body, event.getUserEmail());
    }

    public void sendPaymentSuccessEmail(PaymentEvent event) {
        log.info("Sending payment success email to: {}", event.getUserEmail());

        String htmlContent = buildPaymentSuccessHtmlContent(event);
        String textContent = buildPaymentSuccessTextContent(event);

        Map<String, Object> body = Map.of(
            "from", Map.of("email", fromEmail, "name", fromName),
            "to", new Object[]{
                Map.of("email", event.getUserEmail(), "name", "User")
            },
            "subject", "Payment Successful - Subscription Renewed",
            "html", htmlContent,
            "text", textContent
        );

        sendEmail(body, event.getUserEmail());
    }

    public void sendPaymentFailureEmail(PaymentEvent event) {
        log.info("Sending payment failure email to: {}", event.getUserEmail());

        String htmlContent = buildPaymentFailureHtmlContent(event);
        String textContent = buildPaymentFailureTextContent(event);

        Map<String, Object> body = Map.of(
            "from", Map.of("email", fromEmail, "name", fromName),
            "to", new Object[]{
                Map.of("email", event.getUserEmail(), "name", "User")
            },
            "subject", "Payment Failed - Action Required",
            "html", htmlContent,
            "text", textContent
        );

        sendEmail(body, event.getUserEmail());
    }

    private void sendEmail(Map<String, Object> body, String userEmail) {
        webClient.post()
            .uri("/v1/email")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> log.info("Email sent successfully to {}", userEmail))
            .doOnError(error -> log.error("Failed to send email to {}: {}", userEmail, error.getMessage()))
            .subscribe();
    }

    // Login Email Content Builders
    private String buildLoginHtmlContent(LoginEvent event) {
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

    private String buildLoginTextContent(LoginEvent event) {
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

    // Payment Success Email Content Builders
    private String buildPaymentSuccessHtmlContent(PaymentEvent event) {
        return String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<h2 style='color: #28a745;'>Payment Successful!</h2>" +
            "<p>Hello,</p>" +
            "<p>Your payment has been processed successfully.</p>" +
            "<h3>Payment Details:</h3>" +
            "<ul>" +
            "<li><strong>Subscription ID:</strong> %s</li>" +
            "<li><strong>Amount:</strong> %s %s</li>" +
            "<li><strong>Date:</strong> %s</li>" +
            "</ul>" +
            "<p>Your subscription is now active and will renew automatically on the due date.</p>" +
            "<p>Thank you for your business!</p>" +
            "<p>Best regards,<br>Subnex Team</p>" +
            "</body>" +
            "</html>",
            event.getSubscriptionId(),
            event.getAmount() / 100.0,
            event.getCurrency(),
            event.getTimestamp()
        );
    }

    private String buildPaymentSuccessTextContent(PaymentEvent event) {
        return String.format(
            "Hello,\n\n" +
            "Your payment has been processed successfully.\n\n" +
            "Payment Details:\n" +
            "- Subscription ID: %s\n" +
            "- Amount: %s %s\n" +
            "- Date: %s\n\n" +
            "Your subscription is now active and will renew automatically on the due date.\n\n" +
            "Thank you for your business!\n\n" +
            "Best regards,\n" +
            "Subnex Team",
            event.getSubscriptionId(),
            event.getAmount() / 100.0,
            event.getCurrency(),
            event.getTimestamp()
        );
    }

    // Payment Failure Email Content Builders
    private String buildPaymentFailureHtmlContent(PaymentEvent event) {
        return String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif;'>" +
            "<h2 style='color: #dc3545;'>Payment Failed</h2>" +
            "<p>Hello,</p>" +
            "<p>Unfortunately, your payment could not be processed.</p>" +
            "<h3>Payment Details:</h3>" +
            "<ul>" +
            "<li><strong>Subscription ID:</strong> %s</li>" +
            "<li><strong>Amount:</strong> %s %s</li>" +
            "<li><strong>Reason:</strong> %s</li>" +
            "<li><strong>Date:</strong> %s</li>" +
            "</ul>" +
            "<p>Please update your payment method and try again.</p>" +
            "<p><a href='https://subnex.com/billing'>Update Payment Method</a></p>" +
            "<p>If you need assistance, please contact our support team.</p>" +
            "<p>Best regards,<br>Subnex Team</p>" +
            "</body>" +
            "</html>",
            event.getSubscriptionId(),
            event.getAmount() / 100.0,
            event.getCurrency(),
            event.getReason(),
            event.getTimestamp()
        );
    }

    private String buildPaymentFailureTextContent(PaymentEvent event) {
        return String.format(
            "Hello,\n\n" +
            "Unfortunately, your payment could not be processed.\n\n" +
            "Payment Details:\n" +
            "- Subscription ID: %s\n" +
            "- Amount: %s %s\n" +
            "- Reason: %s\n" +
            "- Date: %s\n\n" +
            "Please update your payment method and try again at: https://subnex.com/billing\n\n" +
            "If you need assistance, please contact our support team.\n\n" +
            "Best regards,\n" +
            "Subnex Team",
            event.getSubscriptionId(),
            event.getAmount() / 100.0,
            event.getCurrency(),
            event.getReason(),
            event.getTimestamp()
        );
    }
}

