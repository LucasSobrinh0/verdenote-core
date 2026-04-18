package com.sobrinholabs.verdenote_core.auth;

public record CsrfResponse(String headerName, String parameterName, String token) {
}
