package br.com.easylink.easylinkservice.infrastructure.api.exception;

import java.time.Instant;

public record ErrorResponse(int status, String error, String message, Instant timestamp) {
}