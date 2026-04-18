package com.sobrinholabs.verdenote_core.admin;

import com.sobrinholabs.verdenote_core.permission.Permission;
import java.util.UUID;

public record PermissionResponse(UUID id, String name, String description) {
	public static PermissionResponse from(Permission permission) {
		return new PermissionResponse(permission.getId(), permission.getName(), permission.getDescription());
	}
}
