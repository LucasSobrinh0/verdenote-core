package com.sobrinholabs.verdenote_core.admin;

import com.sobrinholabs.verdenote_core.auth.AuthUserResponse;
import com.sobrinholabs.verdenote_core.user.User;
import java.util.List;
import java.util.UUID;

public record AdminUserResponse(
		UUID id,
		String firstName,
		String lastName,
		String username,
		String email,
		boolean enabled,
		List<String> groups,
		List<String> permissions) {
	public static AdminUserResponse from(User user) {
		AuthUserResponse response = AuthUserResponse.from(user);
		return new AdminUserResponse(
				response.id(),
				response.firstName(),
				response.lastName(),
				response.username(),
				response.email(),
				response.enabled(),
				response.groups(),
				response.permissions());
	}
}
