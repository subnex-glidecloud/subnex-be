package com.subnex.email.controller;

import com.subnex.email.dto.LoginEvent;
import com.subnex.email.dto.TestEmailRequest;
import com.subnex.email.service.MailerSendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class TestEmailController {

    private final MailerSendService mailerSendService;

    public TestEmailController(MailerSendService mailerSendService) {
        this.mailerSendService = mailerSendService;
    }

    @PostMapping("/api/emails/test")
    public ResponseEntity<String> sendTestEmail(@RequestBody TestEmailRequest request) {
        if (request == null || request.userEmail() == null || request.userEmail().isBlank()) {
            return ResponseEntity.badRequest().body("userEmail is required");
        }

        LoginEvent event = LoginEvent.builder()
            .userEmail(request.userEmail().trim())
            .loginTime(request.loginTime() != null && !request.loginTime().isBlank()
                ? request.loginTime()
                : DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()))
            .ipAddress(request.ipAddress() != null && !request.ipAddress().isBlank() ? request.ipAddress() : "N/A")
            .deviceInfo(request.deviceInfo() != null && !request.deviceInfo().isBlank() ? request.deviceInfo() : "N/A")
            .build();

        mailerSendService.sendLoginNotification(event);
        return ResponseEntity.ok("queued");
    }
}