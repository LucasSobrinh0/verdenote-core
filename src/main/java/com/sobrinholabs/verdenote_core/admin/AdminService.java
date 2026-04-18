package com.sobrinholabs.verdenote_core.admin;

import com.sobrinholabs.verdenote_core.group.Group;
import com.sobrinholabs.verdenote_core.group.GroupRepository;
import com.sobrinholabs.verdenote_core.permission.Permission;
import com.sobrinholabs.verdenote_core.permission.PermissionRepository;
import com.sobrinholabs.verdenote_core.user.User;
import com.sobrinholabs.verdenote_core.user.UserRepository;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminService {
	private final UserRepository userRepository;
	private final GroupRepository groupRepository;
	private final PermissionRepository permissionRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminService(UserRepository userRepository, GroupRepository groupRepository,
			PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.permissionRepository = permissionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public Page<AdminUserResponse> searchUsers(String search, Pageable pageable) {
		if (!StringUtils.hasText(search)) {
			return userRepository.findAll(pageable).map(AdminUserResponse::from);
		}
		return userRepository.search(search.trim(), pageable).map(AdminUserResponse::from);
	}

	@Transactional
	public AdminUserResponse createUser(AdminCreateUserRequest request) {
		String username = request.username().trim().toLowerCase(Locale.ROOT);
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (!request.password().equals(request.confirmPassword())) {
			throw new IllegalArgumentException("As senhas não conferem.");
		}
		if (userRepository.existsByUsernameIgnoreCase(username) || userRepository.existsByEmailIgnoreCase(email)) {
			throw new IllegalArgumentException("Não foi possível criar o usuário com esses dados.");
		}

		Set<Group> groups = resolveGroups(request.groups());
		User user = new User(
				request.firstName().trim(),
				request.lastName().trim(),
				username,
				email,
				passwordEncoder.encode(request.password()),
				groups);
		return AdminUserResponse.from(userRepository.save(user));
	}

	@Transactional
	public AdminUserResponse updateStatus(UUID userId, UpdateUserStatusRequest request) {
		User user = findUser(userId);
		user.setEnabled(request.enabled());
		return AdminUserResponse.from(user);
	}

	@Transactional
	public AdminUserResponse updateGroups(UUID userId, UpdateUserGroupsRequest request) {
		User user = findUser(userId);
		user.replaceGroups(resolveGroups(request.groups()));
		return AdminUserResponse.from(user);
	}

	@Transactional(readOnly = true)
	public java.util.List<GroupResponse> listGroups() {
		return groupRepository.findAll().stream()
				.map(GroupResponse::from)
				.sorted(java.util.Comparator.comparing(GroupResponse::name))
				.toList();
	}

	@Transactional(readOnly = true)
	public java.util.List<PermissionResponse> listPermissions() {
		return permissionRepository.findAll().stream()
				.map(PermissionResponse::from)
				.sorted(java.util.Comparator.comparing(PermissionResponse::name))
				.toList();
	}

	@Transactional
	public GroupResponse createGroup(GroupRequest request) {
		String name = normalizeGroupName(request.name());
		if (groupRepository.existsByNameIgnoreCase(name)) {
			throw new IllegalArgumentException("Já existe um grupo com esse nome.");
		}
		Group group = new Group(name, request.description().trim(), false);
		group.getPermissions().addAll(resolvePermissions(request.permissions()));
		return GroupResponse.from(groupRepository.save(group));
	}

	@Transactional
	public GroupResponse updateGroup(UUID groupId, GroupRequest request) {
		Group group = groupRepository.findById(groupId)
				.orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado."));
		String name = normalizeGroupName(request.name());
		groupRepository.findByNameIgnoreCase(name)
				.filter(existing -> !existing.getId().equals(groupId))
				.ifPresent(existing -> {
					throw new IllegalArgumentException("Já existe um grupo com esse nome.");
				});
		group.update(name, request.description().trim(), resolvePermissions(request.permissions()));
		return GroupResponse.from(group);
	}

	private User findUser(UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
	}

	private Set<Group> resolveGroups(Set<String> requestedGroups) {
		Set<String> names = requestedGroups.stream()
				.map(name -> name.trim().toUpperCase(Locale.ROOT))
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
		if (names.contains("ADMIN")) {
			names.add("USER");
		}
		java.util.List<Group> groups = groupRepository.findByNameIn(names);
		if (groups.size() != names.size()) {
			throw new IllegalArgumentException("Grupo inválido.");
		}
		return new LinkedHashSet<>(groups);
	}

	private Set<Permission> resolvePermissions(Set<String> requestedPermissions) {
		Set<String> names = requestedPermissions.stream()
				.map(name -> name.trim().toUpperCase(Locale.ROOT))
				.collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
		java.util.List<Permission> permissions = permissionRepository.findByNameIn(names);
		if (permissions.size() != names.size()) {
			throw new IllegalArgumentException("Permissão inválida.");
		}
		return new LinkedHashSet<>(permissions);
	}

	private String normalizeGroupName(String name) {
		return name.trim().toUpperCase(Locale.ROOT);
	}
}
