package com.sobrinholabs.verdenote_core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sobrinholabs.verdenote_core.document.DocumentAclRepository;
import com.sobrinholabs.verdenote_core.document.DocumentRepository;
import com.sobrinholabs.verdenote_core.group.Group;
import com.sobrinholabs.verdenote_core.group.GroupRepository;
import com.sobrinholabs.verdenote_core.realtime.RealtimeTicketRepository;
import com.sobrinholabs.verdenote_core.user.User;
import com.sobrinholabs.verdenote_core.user.UserRepository;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentFlowTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private DocumentAclRepository documentAclRepository;

	@Autowired
	private RealtimeTicketRepository realtimeTicketRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cleanDatabase() {
		realtimeTicketRepository.deleteAll();
		documentAclRepository.deleteAll();
		documentRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void createDocumentCreatesOwnerAcl() throws Exception {
		MockHttpSession session = login(createUser("ana", "ana@example.com"));

		MvcResult result = mockMvc.perform(post("/api/documents")
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(documentJson("Plano Verde")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Plano Verde"))
				.andExpect(jsonPath("$.role").value("OWNER"))
				.andExpect(jsonPath("$.permissions[?(@ == 'DOCUMENT_SHARE')]").exists())
				.andReturn();

		String documentId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
		assertThat(documentAclRepository.findByDocumentId(java.util.UUID.fromString(documentId)))
				.extracting(acl -> acl.getRole().name())
				.containsExactly("OWNER");
	}

	@Test
	void userWithoutAclCannotAccessDocument() throws Exception {
		MockHttpSession ownerSession = login(createUser("ana", "ana@example.com"));
		MockHttpSession otherSession = login(createUser("bia", "bia@example.com"));
		String documentId = createDocument(ownerSession, "Privado");

		mockMvc.perform(get("/api/documents/{documentId}", documentId).session(otherSession))
				.andExpect(status().isForbidden());
	}

	@Test
	void viewerCannotRenameDocument() throws Exception {
		User owner = createUser("ana", "ana@example.com");
		User viewer = createUser("bia", "bia@example.com");
		MockHttpSession ownerSession = login(owner);
		MockHttpSession viewerSession = login(viewer);
		String documentId = createDocument(ownerSession, "Leitura");

		mockMvc.perform(post("/api/documents/{documentId}/acl", documentId)
						.with(csrf())
						.session(ownerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content(aclJson(viewer.getId().toString(), "VIEWER")))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/documents/{documentId}", documentId).session(viewerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("VIEWER"));

		mockMvc.perform(patch("/api/documents/{documentId}", documentId)
						.with(csrf())
						.session(viewerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content(documentJson("Tentativa")))
				.andExpect(status().isForbidden());
	}

	@Test
	void editorCanReceiveRealtimeTicket() throws Exception {
		User owner = createUser("ana", "ana@example.com");
		User editor = createUser("bia", "bia@example.com");
		MockHttpSession ownerSession = login(owner);
		MockHttpSession editorSession = login(editor);
		String documentId = createDocument(ownerSession, "Colaborativo");

		mockMvc.perform(post("/api/documents/{documentId}/acl", documentId)
						.with(csrf())
						.session(ownerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content(aclJson(editor.getId().toString(), "EDITOR")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/realtime/tickets")
						.with(csrf())
						.session(editorSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"documentId\":\"%s\"}".formatted(documentId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ticket").isString())
				.andExpect(jsonPath("$.permissions[?(@ == 'DOCUMENT_EDIT')]").exists());
	}

	@Test
	void realtimeValidateEndpointRequiresServiceSecret() throws Exception {
		MockHttpSession session = login(createUser("ana", "ana@example.com"));
		String documentId = createDocument(session, "Realtime");
		MvcResult ticketResult = mockMvc.perform(post("/api/realtime/tickets")
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"documentId\":\"%s\"}".formatted(documentId)))
				.andExpect(status().isOk())
				.andReturn();
		String ticket = JsonPath.read(ticketResult.getResponse().getContentAsString(), "$.ticket");

		String validateBody = "{\"ticket\":\"%s\",\"documentId\":\"%s\"}".formatted(ticket, documentId);
		mockMvc.perform(post("/api/realtime/tickets/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validateBody))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/realtime/tickets/validate")
						.header("X-VerdeNote-Realtime-Secret", "dev-realtime-secret-change-me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("ana"));
	}

	@Test
	void ownerCanShareDocumentByUsernameIdentifier() throws Exception {
		User owner = createUser("ana", "ana@example.com");
		createUser("bia", "bia@example.com");
		MockHttpSession ownerSession = login(owner);
		MockHttpSession viewerSession = login(userRepository.findByUsernameIgnoreCase("bia").orElseThrow());
		String documentId = createDocument(ownerSession, "Compartilhado");

		mockMvc.perform(post("/api/documents/{documentId}/acl", documentId)
						.with(csrf())
						.session(ownerSession)
						.contentType(MediaType.APPLICATION_JSON)
						.content(aclByIdentifierJson("bia", "VIEWER")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("bia"))
				.andExpect(jsonPath("$.role").value("VIEWER"));

		mockMvc.perform(get("/api/documents/{documentId}", documentId).session(viewerSession))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("VIEWER"));
	}

	@Test
	void authenticatedEditorCanPersistDocumentUpdate() throws Exception {
		MockHttpSession session = login(createUser("ana", "ana@example.com"));
		String documentId = createDocument(session, "Autosave");

		mockMvc.perform(post("/api/documents/{documentId}/updates", documentId)
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "updatePayloadBase64": "AQID",
								  "snapshotPayloadBase64": "AQID"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.version").value(1));

		mockMvc.perform(get("/api/documents/{documentId}", documentId).session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentVersion").value(1))
				.andExpect(jsonPath("$.currentSnapshotBase64").value("AQID"));
	}

	@Test
	void anonymousCannotCreateRealtimeTicket() throws Exception {
		mockMvc.perform(post("/api/realtime/tickets")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"documentId\":\"00000000-0000-0000-0000-000000000000\"}"))
				.andExpect(status().isUnauthorized());
	}

	private String createDocument(MockHttpSession session, String title) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/documents")
						.with(csrf())
						.session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content(documentJson(title)))
				.andExpect(status().isCreated())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
	}

	private MockHttpSession login(User user) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.with(csrf())
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.param("identifier", user.getUsername())
						.param("password", "secret123"))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private User createUser(String username, String email) {
		Group userGroup = groupRepository.findByNameIgnoreCase("USER").orElseThrow();
		return userRepository.save(new User("Ana", "Silva", username, email, passwordEncoder.encode("secret123"), Set.of(userGroup)));
	}

	private String documentJson(String title) {
		return "{\"title\":\"%s\"}".formatted(title);
	}

	private String aclJson(String userId, String role) {
		return """
				{
				  "userId": "%s",
				  "role": "%s"
				}
				""".formatted(userId, role);
	}

	private String aclByIdentifierJson(String identifier, String role) {
		return """
				{
				  "userIdentifier": "%s",
				  "role": "%s"
				}
				""".formatted(identifier, role);
	}
}
