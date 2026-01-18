package com.subnex.email.dto;

public record TestEmailRequest(
	String userEmail,
	String loginTime,
	String ipAddress,
	String deviceInfo
) {
}