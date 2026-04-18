package com.sobrinholabs.verdenote_core.admin;

import com.sobrinholabs.verdenote_core.audit.LoginAuditEventResponse;
import com.sobrinholabs.verdenote_core.audit.LoginAuditService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final AdminService adminService;
	private final LoginAuditService loginAuditService;

	public AdminController(AdminService adminService, LoginAuditService loginAuditService) {
		this.adminService = adminService;
		this.loginAuditService = loginAuditService;
	}

	@GetMapping("/dashboard")
	public java.util.Map<String, String> dashboard() {
		return java.util.Map.of("message", "admin ok");
	}

	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_READ')")
	public Page<AdminUserResponse> users(
			@RequestParam(required = false) String search,
			@PageableDefault(size = 20) Pageable pageable) {
		return adminService.searchUsers(search, pageable);
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CREATE')")
	public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminCreateUserRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
	}

	@PatchMapping("/users/{userId}/status")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
	public AdminUserResponse updateStatus(@PathVariable UUID userId, @Valid @RequestBody UpdateUserStatusRequest request) {
		return adminService.updateStatus(userId, request);
	}

	@PutMapping("/users/{userId}/groups")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_UPDATE')")
	public AdminUserResponse updateGroups(@PathVariable UUID userId, @Valid @RequestBody UpdateUserGroupsRequest request) {
		return adminService.updateGroups(userId, request);
	}

	@GetMapping("/groups")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('GROUP_READ')")
	public List<GroupResponse> groups() {
		return adminService.listGroups();
	}

	@PostMapping("/groups")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('GROUP_MANAGE')")
	public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createGroup(request));
	}

	@PutMapping("/groups/{groupId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('GROUP_MANAGE')")
	public GroupResponse updateGroup(@PathVariable UUID groupId, @Valid @RequestBody GroupRequest request) {
		return adminService.updateGroup(groupId, request);
	}

	@GetMapping("/permissions")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('GROUP_READ')")
	public List<PermissionResponse> permissions() {
		return adminService.listPermissions();
	}

	@GetMapping("/audit/login-events")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('AUDIT_READ')")
	public Page<LoginAuditEventResponse> loginEvents(
			@RequestParam(required = false) String search,
			@PageableDefault(size = 30) Pageable pageable) {
		return loginAuditService.search(search, pageable);
	}
}
