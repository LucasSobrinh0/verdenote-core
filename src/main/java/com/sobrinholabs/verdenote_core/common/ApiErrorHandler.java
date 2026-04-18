package com.sobrinholabs.verdenote_core.common;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<Map<String, String>> handleValidation() {
		return ResponseEntity.badRequest().body(Map.of("message", "Dados inválidos."));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", exception.getMessage()));
	}

	@ExceptionHandler(SecurityException.class)
	ResponseEntity<Map<String, String>> handleSecurityException() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acesso negado."));
	}
}
