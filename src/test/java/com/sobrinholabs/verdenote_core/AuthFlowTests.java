package com.sobrinholabs.verdenote_core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sobrinholabs.verdenote_core.auth.LoginAttemptService;
import com.sobrinholabs.verdenote_core.document.DocumentRepository;
import com.sobrinholabs.verdenote_core.group.Group;
import com.sobrinholabs.verdenote_core.group.GroupRepository;
import com.sobrinholabs.verdenote_core.user.User;
import com.sobrinholabs.verdenote_core.user.UserRepository;
import java.util.Set;
import org.springframework.mock.web.MockHttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Autowired
	private DocumentRepository documentRepository;

	@BeforeEach
	void cleanDatabase() {
		loginAttemptService.clear();
		documentRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void registerCreatesUserWithEncodedPassword() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerJson("ana", "ana@example.com")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("Usuário criado com sucesso"))
				.andExpect(jsonPath("$.user.groups[0]").value("USER"));

		User user = userRepository.findByUsernameIgnoreCase("ana").orElseThrow();
		assertThat(user.getGroups()).extracting(Group::getName).containsExactly("USER");
		assertThat(user.getPasswordHash()).isNotEqualTo("secret123");
		assertThat(passwordEncoder.matches("secret123", user.getPasswordHash())).isTrue();
	}

	@Test
	void duplicateUsernameReturnsGenericConflict() throws Exception {
		createUser("ana", "ana@example.com");

		mockMvc.perform(post("/api/auth/register")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerJson("ana", "another@example.com")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Não foi possível criar o usuário com esses dados."));
	}

	@Test
	void loginWithUsernameAndEmailCanAccessProtectedEndpoint() throws Exception {
		createUser("ana", "ana@example.com");

		MvcResult usernameLogin = mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "ana")
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ana"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) usernameLogin.getRequest().getSession(false);
		mockMvc.perform(get("/api/user/profile").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("ana@example.com"));

		mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "ana@example.com")
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ana"));
	}

	@Test
	void csrfIsRequiredForStateChangingRequests() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerJson("ana", "ana@example.com")))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "ana")
						.param("password", "secret123"))
				.andExpect(status().isForbidden());
	}

	@Test
	void authorizationRulesProtectUserAndAdminEndpoints() throws Exception {
		mockMvc.perform(get("/api/user/profile"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/admin/ping").with(user("ana").roles("USER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminRoleCanOpenAdminPanelRoutesEvenWithoutFineGrainedAuthorities() throws Exception {
		mockMvc.perform(get("/api/admin/users").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/groups").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/admin/audit/login-events").with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk());
	}

	@Test
	void repeatedFailedLoginsAreTemporarilyBlocked() throws Exception {
		createUser("ana", "ana@example.com");

		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post("/api/auth/login")
							.with(csrf())
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.param("identifier", "ana")
							.param("password", "wrong"))
					.andExpect(status().isUnauthorized());
		}

		mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "ana")
						.param("password", "secret123"))
				.andExpect(status().isTooManyRequests());
	}

	@Test
	void logoutInvalidatesSessionAndClearsCookies() throws Exception {
		createUser("ana", "ana@example.com");
		MvcResult login = mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "ana")
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
				.andExpect(status().isNoContent())
				.andExpect(cookie().maxAge("VERDENOTE_SESSION", 0));

		mockMvc.perform(get("/api/user/profile").session(session))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCanListCurrentUserAndCreatedUsers() throws Exception {
		createUser("lucas", "lucas@example.com", "ADMIN", "USER");
		MvcResult login = mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "lucas")
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.groups").isArray())
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		mockMvc.perform(get("/api/admin/users").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].username").value("lucas"))
				.andExpect(jsonPath("$.content[0].groups[0]").value("ADMIN"));

		mockMvc.perform(post("/api/admin/users")
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(adminCreateUserJson()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("bia"))
				.andExpect(jsonPath("$.groups[0]").value("USER"));

		mockMvc.perform(get("/api/admin/users").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.content[?(@.username == 'lucas')]").exists())
				.andExpect(jsonPath("$.content[?(@.username == 'bia')]").exists());
	}

	@Test
	void adminCanCreateAndEditGroups() throws Exception {
		createUser("lucas", "lucas@example.com", "ADMIN", "USER");
		MvcResult login = mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", "lucas")
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
		MvcResult createGroup = mockMvc.perform(post("/api/admin/groups")
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(groupJson("support", "Atendimento", "APP_ACCESS", "PROFILE_READ")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("SUPPORT"))
				.andExpect(jsonPath("$.systemGroup").value(false))
				.andReturn();

		String groupId = com.jayway.jsonpath.JsonPath.read(createGroup.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(put("/api/admin/groups/{groupId}", groupId)
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(groupJson("support_ops", "Atendimento operacional", "APP_ACCESS", "PROFILE_READ", "USER_READ")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("SUPPORT_OPS"))
				.andExpect(jsonPath("$.description").value("Atendimento operacional"))
				.andExpect(jsonPath("$.permissions[?(@ == 'USER_READ')]").exists());
	}

	private void createUser(String username, String email) {
		createUser(username, email, "USER");
	}

	private void createUser(String username, String email, String... groups) {
		Set<Group> userGroups = java.util.Arrays.stream(groups)
				.map(group -> groupRepository.findByNameIgnoreCase(group).orElseThrow())
				.collect(java.util.stream.Collectors.toSet());
		userRepository.save(new User("Ana", "Silva", username, email, passwordEncoder.encode("secret123"), userGroups));
	}

	private String registerJson(String username, String email) {
		return """
				{
				  "firstName": "Ana",
				  "lastName": "Silva",
				  "username": "%s",
				  "email": "%s",
				  "password": "secret123",
				  "confirmPassword": "secret123"
				}
				""".formatted(username, email);
	}

	private String adminCreateUserJson() {
		return """
				{
				  "firstName": "Bia",
				  "lastName": "Souza",
				  "username": "bia",
				  "email": "bia@example.com",
				  "password": "secret123",
				  "confirmPassword": "secret123",
				  "groups": ["USER"]
				}
				""";
	}

	private String groupJson(String name, String description, String... permissions) {
		String permissionJson = java.util.Arrays.stream(permissions)
				.map(permission -> "\"" + permission + "\"")
				.collect(java.util.stream.Collectors.joining(", "));
		return """
				{
				  "name": "%s",
				  "description": "%s",
				  "permissions": [%s]
				}
				""".formatted(name, description, permissionJson);
	}
}
