package com.sobrinholabs.verdenote_core.security;

import com.sobrinholabs.verdenote_core.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final UserService userService;

	public CustomUserDetailsService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public UserDetails loadUserByUsername(String identifier) {
		return userService.findByUsernameOrEmail(identifier)
				.map(CurrentUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas."));
	}
}
