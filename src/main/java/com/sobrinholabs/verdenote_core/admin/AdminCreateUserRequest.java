package com.sobrinholabs.verdenote_core.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminCreateUserRequest(
		@NotBlank @Size(max = 80) String firstName,
		@NotBlank @Size(max = 80) String lastName,
		@NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$") String username,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 72) String password,
		@NotBlank @Size(min = 8, max = 72) String confirmPassword,
		@NotEmpty Set<@NotBlank String> groups) {
}
