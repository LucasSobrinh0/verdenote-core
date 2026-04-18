package com.sobrinholabs.verdenote_core.user;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public Optional<User> findByUsernameOrEmail(String identifier) {
		return userRepository.findByUsernameIgnoreCase(identifier)
				.or(() -> userRepository.findByEmailIgnoreCase(identifier));
	}
}
