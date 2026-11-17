package br.com.certifai.exception.config;

import br.com.certifai.exception.ConflitoException;
import br.com.certifai.exception.EntidadeNaoEncontradaException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest httpServletRequest = servletWebRequest.getRequest();
            return httpServletRequest.getRequestURI();
        }
        String description = request.getDescription(false);
        return description != null && description.startsWith("uri=") ? description.substring(4) : description;
    }

    @ExceptionHandler({EntidadeNaoEncontradaException.class, EntityNotFoundException.class})
    public ResponseEntity<ErrorDetails> handleEntidadeNaoEncontrada(Exception ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("Entidade não encontrada [{}]: {}", path, ex.getMessage());
        ErrorDetails error = new ErrorDetails(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorDetails> handleConflito(ConflitoException ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("Conflito de dados [{}]: {}", path, ex.getMessage());
        ErrorDetails error = new ErrorDetails(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String path = extractPath(request);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String fieldName = err instanceof FieldError fieldError ? fieldError.getField() : err.getObjectName();
            String errorMessage = err.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Erro de validação de argumentos [{}]: {}", path, errors);
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Falha na validação dos campos da requisição",
                path,
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetails> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        String path = extractPath(request);
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        log.warn("Violação de restrição [{}]: {}", path, errors);
        ErrorDetails error = new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Violação de restrições de validação",
                path,
                errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorDetails> handleIllegalArgument(RuntimeException ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("Requisição inválida [{}]: {}", path, ex.getMessage());
        ErrorDetails error = new ErrorDetails(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(), path);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("Tentativa de autenticação com credenciais inválidas [{}]", path);
        ErrorDetails error = new ErrorDetails(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Credenciais inválidas. Verifique seu e-mail e senha.", path);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        String path = extractPath(request);
        log.warn("Acesso negado [{}]: {}", path, ex.getMessage());
        ErrorDetails error = new ErrorDetails(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(), "Acesso não autorizado para o recurso solicitado.", path);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        String path = extractPath(request);
        log.error("Erro interno não tratado [{}]: ", path, ex);
        ErrorDetails error = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Ocorreu um erro interno inesperado no servidor.", path);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
