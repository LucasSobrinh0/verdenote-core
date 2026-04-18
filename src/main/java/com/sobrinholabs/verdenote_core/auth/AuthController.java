package com.sobrinholabs.verdenote_core.auth;

import com.sobrinholabs.verdenote_core.audit.ActionLogService;
import com.sobrinholabs.verdenote_core.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
	private final AuthService authService;
	private final ActionLogService actionLogService;

	public AuthController(AuthService authService, ActionLogService actionLogService) {
		this.authService = authService;
		this.actionLogService = actionLogService;
	}

	@GetMapping("/csrf")
	public CsrfResponse csrf(CsrfToken csrfToken) {
		return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getParameterName(), csrfToken.getToken());
	}

	@PostMapping("/auth/register")
	public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
		AuthUserResponse user = authService.register(request);
		actionLogService.log(servletRequest, null, "REGISTER_SUCCESS", Map.of("username", user.username()));
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("message", "Usuário criado com sucesso", "user", user));
	}

	@GetMapping("/auth/me")
	public AuthUserResponse me(@AuthenticationPrincipal CurrentUser currentUser, HttpServletRequest request) {
		actionLogService.log(request, null, "AUTH_ME_SUCCESS", Map.of(
				"username", currentUser.getUsername(),
				"groups", currentUser.getUser().getGroups().stream().map(group -> group.getName()).sorted().toList()));
		return AuthUserResponse.from(currentUser.getUser());
	}

	@GetMapping("/user/profile")
	public AuthUserResponse profile(@AuthenticationPrincipal CurrentUser currentUser, HttpServletRequest request) {
		actionLogService.log(request, null, "USER_PROFILE", Map.of("username", currentUser.getUsername()));
		return AuthUserResponse.from(currentUser.getUser());
	}

	@GetMapping("/admin/ping")
	public Map<String, String> adminPing() {
		return Map.of("message", "admin ok");
	}
}
