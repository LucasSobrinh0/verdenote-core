package com.sobrinholabs.verdenote_core.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record GroupRequest(
		@NotBlank @Size(min = 2, max = 60) @Pattern(regexp = "^[a-zA-Z0-9_-]+$") String name,
		@NotBlank @Size(max = 255) String description,
		@NotEmpty Set<@NotBlank String> permissions) {
}
