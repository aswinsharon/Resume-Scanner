package com.jobboard.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Resource not found - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "RESOURCE_NOT_FOUND",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ErrorResponse> handleBadRequestException(
                        BadRequestException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Bad request - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "BAD_REQUEST",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Unauthorized access - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "UNAUTHORIZED",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ErrorResponse> handleForbiddenException(
                        ForbiddenException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Forbidden access - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "FORBIDDEN",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ErrorResponse> handleConflictException(
                        ConflictException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Conflict - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "CONFLICT",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(FileProcessingException.class)
        public ResponseEntity<ErrorResponse> handleFileProcessingException(
                        FileProcessingException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.error("File processing error - TraceId: {}, Message: {}", traceId, ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "FILE_PROCESSING_ERROR",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        @ExceptionHandler(ServiceUnavailableException.class)
        public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
                        ServiceUnavailableException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.error("Service unavailable - TraceId: {}, Message: {}", traceId, ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "SERVICE_UNAVAILABLE",
                                ex.getMessage(),
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Validation error - TraceId: {}", traceId);

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ErrorResponse errorResponse = new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Invalid input data",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setDetails(errors);
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolationException(
                        ConstraintViolationException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Constraint violation - TraceId: {}", traceId);

                Map<String, String> errors = new HashMap<>();
                for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
                        String fieldName = violation.getPropertyPath().toString();
                        String errorMessage = violation.getMessage();
                        errors.put(fieldName, errorMessage);
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                "VALIDATION_ERROR",
                                "Constraint violation",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setDetails(errors);
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
                        MethodArgumentTypeMismatchException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Method argument type mismatch - TraceId: {}, Parameter: {}", traceId, ex.getName());

                String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());

                ErrorResponse errorResponse = new ErrorResponse(
                                "INVALID_PARAMETER_TYPE",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
                        MissingServletRequestParameterException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Missing request parameter - TraceId: {}, Parameter: {}", traceId, ex.getParameterName());

                String message = String.format("Required parameter '%s' is missing", ex.getParameterName());

                ErrorResponse errorResponse = new ErrorResponse(
                                "MISSING_PARAMETER",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
                        HttpMessageNotReadableException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Malformed JSON request - TraceId: {}", traceId);

                ErrorResponse errorResponse = new ErrorResponse(
                                "MALFORMED_JSON",
                                "Malformed JSON request",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
                        HttpRequestMethodNotSupportedException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Method not supported - TraceId: {}, Method: {}", traceId, ex.getMethod());

                String message = String.format("Request method '%s' not supported", ex.getMethod());

                ErrorResponse errorResponse = new ErrorResponse(
                                "METHOD_NOT_SUPPORTED",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
        }

        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
                        HttpMediaTypeNotSupportedException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Media type not supported - TraceId: {}, MediaType: {}", traceId, ex.getContentType());

                String message = String.format("Media type '%s' not supported", ex.getContentType());

                ErrorResponse errorResponse = new ErrorResponse(
                                "MEDIA_TYPE_NOT_SUPPORTED",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
                        MaxUploadSizeExceededException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("File size exceeded - TraceId: {}", traceId);

                ErrorResponse errorResponse = new ErrorResponse(
                                "FILE_SIZE_EXCEEDED",
                                "File size exceeds maximum allowed limit",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.PAYLOAD_TOO_LARGE);
        }

        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
                        NoHandlerFoundException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("No handler found - TraceId: {}, URL: {}", traceId, ex.getRequestURL());

                String message = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());

                ErrorResponse errorResponse = new ErrorResponse(
                                "ENDPOINT_NOT_FOUND",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
                        DataIntegrityViolationException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.error("Data integrity violation - TraceId: {}", traceId, ex);

                String message = "Data integrity violation. Please check your input data.";
                if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
                        message = "Duplicate entry. The resource already exists.";
                }

                ErrorResponse errorResponse = new ErrorResponse(
                                "DATA_INTEGRITY_VIOLATION",
                                message,
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentialsException(
                        BadCredentialsException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Bad credentials - TraceId: {}", traceId);

                ErrorResponse errorResponse = new ErrorResponse(
                                "INVALID_CREDENTIALS",
                                "Invalid email or password",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthenticationException(
                        AuthenticationException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Authentication failed - TraceId: {}, Message: {}", traceId, ex.getMessage());

                ErrorResponse errorResponse = new ErrorResponse(
                                "AUTHENTICATION_FAILED",
                                "Authentication failed",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(
                        AccessDeniedException ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.warn("Access denied - TraceId: {}", traceId);

                ErrorResponse errorResponse = new ErrorResponse(
                                "ACCESS_DENIED",
                                "You don't have permission to access this resource",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex, WebRequest request) {

                String traceId = generateTraceId();
                logger.error("Unexpected error - TraceId: {}, Message: {}", traceId, ex.getMessage(), ex);

                ErrorResponse errorResponse = new ErrorResponse(
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred. Please try again later.",
                                LocalDateTime.now(),
                                request.getDescription(false));
                errorResponse.setTraceId(traceId);

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        private String generateTraceId() {
                return UUID.randomUUID().toString().substring(0, 8);
        }
}