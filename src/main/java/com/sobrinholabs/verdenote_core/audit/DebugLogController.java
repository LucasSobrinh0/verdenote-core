package com.sobrinholabs.verdenote_core.audit;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugLogController {
	private final ActionLogService actionLogService;

	public DebugLogController(ActionLogService actionLogService) {
		this.actionLogService = actionLogService;
	}

	@PostMapping("/frontend-log")
	public ResponseEntity<Void> frontendLog(@Valid @RequestBody FrontendLogRequest request) {
		actionLogService.logFrontend(request);
		return ResponseEntity.noContent().build();
	}
}
