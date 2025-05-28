// Path: easylink-service/src/main/java/br/com/easylink/easylinkservice/infrastructure/api/exception/GlobalExceptionHandler.java
package br.com.easylink.easylinkservice.infrastructure.api.exception;

import br.com.easylink.easylinkservice.application.exceptions.UrlNotFoundException;
import br.com.easylink.easylinkservice.application.exceptions.UserNotAuthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlNotFound(UrlNotFoundException ex) {
        var status = HttpStatus.NOT_FOUND;
        var errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserNotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUserNotAuthorized(UserNotAuthorizedException ex) {
        var status = HttpStatus.FORBIDDEN;
        var errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }
}