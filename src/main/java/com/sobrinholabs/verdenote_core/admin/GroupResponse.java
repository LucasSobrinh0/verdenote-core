package com.sobrinholabs.verdenote_core.admin;

import com.sobrinholabs.verdenote_core.group.Group;
import java.util.List;
import java.util.UUID;

public record GroupResponse(
		UUID id,
		String name,
		String description,
		boolean systemGroup,
		List<String> permissions) {
	public static GroupResponse from(Group group) {
		return new GroupResponse(
				group.getId(),
				group.getName(),
				group.getDescription(),
				group.isSystemGroup(),
				group.getPermissions().stream().map(permission -> permission.getName()).sorted().toList());
	}
}
