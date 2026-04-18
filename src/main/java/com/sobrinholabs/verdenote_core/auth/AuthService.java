package com.sobrinholabs.verdenote_core.auth;

import com.sobrinholabs.verdenote_core.group.Group;
import com.sobrinholabs.verdenote_core.group.GroupRepository;
import com.sobrinholabs.verdenote_core.user.User;
import com.sobrinholabs.verdenote_core.user.UserRepository;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UserRepository userRepository, GroupRepository groupRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AuthUserResponse register(RegisterRequest request) {
		String username = request.username().trim().toLowerCase(Locale.ROOT);
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (!request.password().equals(request.confirmPassword())) {
			throw new IllegalArgumentException("As senhas não conferem.");
		}
		if (userRepository.existsByUsernameIgnoreCase(username)) {
			throw new IllegalArgumentException("Não foi possível criar o usuário com esses dados.");
		}
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException("Não foi possível criar o usuário com esses dados.");
		}

		Group userGroup = groupRepository.findByNameIgnoreCase("USER")
				.orElseThrow(() -> new IllegalStateException("Grupo USER não foi encontrado."));
		User user = new User(
				request.firstName().trim(),
				request.lastName().trim(),
				username,
				email,
				passwordEncoder.encode(request.password()),
				Set.of(userGroup));

		return AuthUserResponse.from(userRepository.save(user));
	}
}
