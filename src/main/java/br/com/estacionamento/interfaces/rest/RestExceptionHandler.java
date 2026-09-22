package br.com.estacionamento.interfaces.rest;

import br.com.estacionamento.application.exception.ActiveStayAlreadyExistsException;
import br.com.estacionamento.application.exception.ActiveStayNotFoundException;
import br.com.estacionamento.application.exception.AdministrativeAccessDeniedException;
import br.com.estacionamento.application.exception.ParkingLotFullException;
import br.com.estacionamento.application.exception.StayNotFoundException;
import br.com.estacionamento.interfaces.rest.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler({ActiveStayAlreadyExistsException.class, ParkingLotFullException.class})
    ResponseEntity<ApiError> handleConflict(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "A operação conflita com o estado atual do estacionamento", request, Map.of());
    }

    @ExceptionHandler({ActiveStayNotFoundException.class, StayNotFoundException.class})
    ResponseEntity<ApiError> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AdministrativeAccessDeniedException.class)
    ResponseEntity<ApiError> handleForbidden(AdministrativeAccessDeniedException exception, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> violations = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> violations.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Dados da requisição inválidos", request, violations);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    ResponseEntity<ApiError> handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Formato da requisição inválido", request, Map.of());
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> violations) {
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                violations);
        return ResponseEntity.status(status).body(error);
    }
}
