package com.sobrinholabs.verdenote_core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sobrinholabs.verdenote_core.auth.LoginAttemptService;
import com.sobrinholabs.verdenote_core.audit.ActionLogService;
import com.sobrinholabs.verdenote_core.audit.LoginAuditService;
import java.util.Arrays;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			AuthenticationSuccessHandler authenticationSuccessHandler,
			AuthenticationFailureHandler authenticationFailureHandler,
			LogoutSuccessHandler logoutSuccessHandler,
			LoginAttemptService loginAttemptService,
			LoginAuditService loginAuditService,
			ActionLogService actionLogService,
			ObjectMapper objectMapper,
			PersistentTokenRepository persistentTokenRepository,
			@Value("${spring.security.remember-me.key}") String rememberMeKey,
			@Value("${verdenote.remember-me.cookie-name:VERDENOTE_REMEMBER_ME}") String rememberMeCookieName,
			@Value("${verdenote.remember-me.validity-seconds:2592000}") int rememberMeValiditySeconds) throws Exception {
		HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
		csrfTokenRepository.setHeaderName("X-CSRF-TOKEN");

		http
				.cors(Customizer.withDefaults())
				.csrf(csrf -> csrf
						.csrfTokenRepository(csrfTokenRepository)
						.ignoringRequestMatchers(
								"/api/realtime/tickets/validate",
								"/api/realtime/documents/*/updates"))
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.sessionFixation(sessionFixation -> sessionFixation.changeSessionId()))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/error").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/debug/frontend-log").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/realtime/tickets/validate").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/realtime/documents/*/updates").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
						.requestMatchers("/api/documents/**", "/api/realtime/tickets").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
						.anyRequest().denyAll())
				.formLogin(form -> form
						.loginProcessingUrl("/api/auth/login")
						.usernameParameter("identifier")
						.passwordParameter("password")
						.successHandler(authenticationSuccessHandler)
						.failureHandler(authenticationFailureHandler))
				.logout(logout -> logout
						.logoutUrl("/api/auth/logout")
						.invalidateHttpSession(true)
						.clearAuthentication(true)
						.deleteCookies("VERDENOTE_SESSION", rememberMeCookieName)
						.logoutSuccessHandler(logoutSuccessHandler))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) -> {
							actionLogService.log(request, null, "AUTHENTICATION_ENTRY_POINT", Map.of(
									"status", 401,
									"reason", exception.getClass().getSimpleName()));
							response.setStatus(401);
							response.setContentType("application/json");
							objectMapper.writeValue(response.getWriter(), Map.of("message", "Autenticação necessária."));
						})
						.accessDeniedHandler((request, response, exception) -> {
							actionLogService.log(request,
									org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(),
									"ACCESS_DENIED", Map.of(
											"status", 403,
											"reason", exception.getClass().getSimpleName()));
							response.setStatus(403);
							response.setContentType("application/json");
							objectMapper.writeValue(response.getWriter(), Map.of("message", "Acesso negado."));
						}))
				.rememberMe(rememberMe -> rememberMe
						.rememberMeParameter("remember-me")
						.rememberMeCookieName(rememberMeCookieName)
						.tokenRepository(persistentTokenRepository)
						.key(rememberMeKey)
						.tokenValiditySeconds(rememberMeValiditySeconds))
				.headers(headers -> headers
						.contentTypeOptions(Customizer.withDefaults())
						.frameOptions(frame -> frame.deny())
						.referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
						.httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
						.contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
						.permissionsPolicyHeader(permissions -> permissions.policy("geolocation=(), microphone=(), camera=()")))
				.httpBasic(AbstractHttpConfigurer::disable);

		http.addFilterBefore(new LoginRateLimitFilter(loginAttemptService, objectMapper), UsernamePasswordAuthenticationFilter.class);
		http.addFilterAfter(new SessionIpTrackingFilter(loginAuditService), UsernamePasswordAuthenticationFilter.class);
		http.addFilterAfter(new ActionLoggingFilter(actionLogService), SessionIpTrackingFilter.class);

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	PersistentTokenRepository persistentTokenRepository(DataSource dataSource) {
		JdbcTokenRepositoryImpl repository = new JdbcTokenRepositoryImpl();
		repository.setDataSource(dataSource);
		return repository;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(@Value("${verdenote.cors.allowed-origins}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList());
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Content-Type", "X-CSRF-TOKEN"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
