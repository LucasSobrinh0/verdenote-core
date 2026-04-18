package com.sobrinholabs.verdenote_core.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
	private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();
	private final int maxAttempts;
	private final Duration lockDuration;
	private final Clock clock;

	@Autowired
	public LoginAttemptService(
			@Value("${verdenote.login-attempt.max-attempts:5}") int maxAttempts,
			@Value("${verdenote.login-attempt.lock-minutes:15}") long lockMinutes) {
		this(maxAttempts, Duration.ofMinutes(lockMinutes), Clock.systemUTC());
	}

	LoginAttemptService(int maxAttempts, Duration lockDuration, Clock clock) {
		this.maxAttempts = maxAttempts;
		this.lockDuration = lockDuration;
		this.clock = clock;
	}

	public boolean isBlocked(String identifier, String remoteAddress) {
		Attempt attempt = attempts.get(key(identifier, remoteAddress));
		if (attempt == null || attempt.lockedUntil == null) {
			return false;
		}
		if (Instant.now(clock).isAfter(attempt.lockedUntil)) {
			attempts.remove(key(identifier, remoteAddress));
			return false;
		}
		return true;
	}

	public void loginSucceeded(String identifier, String remoteAddress) {
		attempts.remove(key(identifier, remoteAddress));
	}

	public void loginFailed(String identifier, String remoteAddress) {
		String key = key(identifier, remoteAddress);
		attempts.compute(key, (ignored, previous) -> {
			int failures = previous == null ? 1 : previous.failures + 1;
			Instant lockedUntil = failures >= maxAttempts ? Instant.now(clock).plus(lockDuration) : null;
			return new Attempt(failures, lockedUntil);
		});
	}

	public void clear() {
		attempts.clear();
	}

	private String key(String identifier, String remoteAddress) {
		return normalize(identifier) + "|" + normalize(remoteAddress);
	}

	private String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	private record Attempt(int failures, Instant lockedUntil) {
	}
}
