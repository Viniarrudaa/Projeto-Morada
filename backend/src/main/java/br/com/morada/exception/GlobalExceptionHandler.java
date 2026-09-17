package br.com.morada.exception;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException exception) {
        return build(exception.getStatus(), exception.getCode(), exception.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

        return build(HttpStatus.BAD_REQUEST, "validation_error", "Revise os campos enviados.", details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied() {
        return build(HttpStatus.FORBIDDEN, "forbidden", "Você não tem permissão para esta ação.", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "internal_error",
            "Não foi possível concluir a operação.",
            List.of()
        );
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, List<String> details) {
        ApiError error = new ApiError(Instant.now(), status.value(), code, message, details);
        return ResponseEntity.status(status).body(error);
    }
}
