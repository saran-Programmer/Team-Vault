package com.teamvault.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.teamvault.DTO.ExceptionResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandling {

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<ExceptionResponse> handleUserExistsException(
            UserExistsException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ExceptionResponse> handleTokenException(
            TokenException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errors = ex.getBindingResult()
                          .getFieldErrors()
                          .stream()
                          .map(FieldError::getDefaultMessage)
                          .collect(Collectors.joining("; "));

        ExceptionResponse body = ExceptionResponse.builder()
                .message(errors)
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType("VALIDATION_ERROR")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    
    @ExceptionHandler(InvalidActionException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidActionException(
            InvalidActionException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message("You are not allowed to perform this action")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType("ACCESS_DENIED")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage() != null ? ex.getMessage() : "Internal server error")
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType("INTERNAL_SERVER_ERROR")
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ExceptionResponse> handleS3Exception(
    		S3Exception ex, HttpServletRequest request) {

        ExceptionResponse body = ExceptionResponse.builder()
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .errorType(ex.getErrorType())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
