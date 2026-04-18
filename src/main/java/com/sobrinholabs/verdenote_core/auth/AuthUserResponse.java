package com.sobrinholabs.verdenote_core.auth;

import com.sobrinholabs.verdenote_core.user.User;
import java.util.List;
import java.util.UUID;

public record AuthUserResponse(
		UUID id,
		String firstName,
		String lastName,
		String username,
		String email,
		boolean enabled,
		List<String> groups,
		List<String> permissions) {
	public static AuthUserResponse from(User user) {
		return new AuthUserResponse(
				user.getId(),
				user.getFirstName(),
				user.getLastName(),
				user.getUsername(),
				user.getEmail(),
				user.isEnabled(),
				user.getGroups().stream().map(group -> group.getName()).sorted().toList(),
				user.getGroups().stream()
						.flatMap(group -> group.getPermissions().stream())
						.map(permission -> permission.getName())
						.distinct()
						.sorted()
						.toList());
	}
}
