package com.sobrinholabs.verdenote_core.security;

import com.sobrinholabs.verdenote_core.user.User;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CurrentUser implements UserDetails {
	private final User user;

	public CurrentUser(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		user.getGroups().forEach(group -> {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + group.getName()));
			group.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission.getName())));
		});
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}
