package com.sobrinholabs.verdenote_core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.servlet.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityCookieConfig {
	@Bean
	CookieSameSiteSupplier sessionCookieSameSiteSupplier(
			@Value("${verdenote.remember-me.cookie-name:VERDENOTE_REMEMBER_ME}") String rememberMeCookieName) {
		return CookieSameSiteSupplier.ofLax().whenHasNameMatching("VERDENOTE_SESSION|" + rememberMeCookieName);
	}
}
