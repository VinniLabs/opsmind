package com.vinnilabs.opsmind.adapter.rest.error;

import com.vinnilabs.opsmind.application.exception.AiAnalysisException;
import com.vinnilabs.opsmind.application.exception.AiProvidersUnavailableException;
import com.vinnilabs.opsmind.application.exception.AnalysisNotFoundException;
import com.vinnilabs.opsmind.application.exception.ApplicationException;
import com.vinnilabs.opsmind.application.exception.IncidentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AnalysisNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisNotFound(AnalysisNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIncidentNotFound(IncidentNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysis(AiAnalysisException exception) {
        return buildResponse(HttpStatus.BAD_GATEWAY, exception);
    }

    @ExceptionHandler(AiProvidersUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleAiProvidersUnavailable(AiProvidersUnavailableException exception) {
        log.warn("All AI providers unavailable: {}", exception.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplication(ApplicationException exception) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        ErrorResponse body = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        log.error("Unexpected error", exception);

        ErrorResponse body = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, ApplicationException exception) {
        ErrorResponse body = ErrorResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
