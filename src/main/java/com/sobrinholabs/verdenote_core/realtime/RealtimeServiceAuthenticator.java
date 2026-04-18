package com.sobrinholabs.verdenote_core.realtime;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RealtimeServiceAuthenticator {
	public static final String HEADER_NAME = "X-VerdeNote-Realtime-Secret";

	private final String serviceSecret;

	public RealtimeServiceAuthenticator(@Value("${verdenote.realtime.service-secret}") String serviceSecret) {
		this.serviceSecret = serviceSecret;
	}

	public void requireValidSecret(HttpServletRequest request) {
		String providedSecret = request.getHeader(HEADER_NAME);
		if (!StringUtils.hasText(serviceSecret) || !StringUtils.hasText(providedSecret)
				|| !MessageDigest.isEqual(bytes(serviceSecret), bytes(providedSecret))) {
			throw new SecurityException("Acesso negado.");
		}
	}

	private byte[] bytes(String value) {
		return value.getBytes(StandardCharsets.UTF_8);
	}
}
