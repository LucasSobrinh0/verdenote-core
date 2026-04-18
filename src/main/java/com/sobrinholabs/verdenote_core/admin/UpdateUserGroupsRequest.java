package com.sobrinholabs.verdenote_core.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UpdateUserGroupsRequest(@NotEmpty Set<@NotBlank String> groups) {
}
