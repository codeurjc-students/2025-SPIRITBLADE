package com.tfg.tfg.unit;

import com.tfg.tfg.exception.*;
import com.tfg.tfg.exception.GlobalExceptionHandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler handler;
    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void testHandleUserAlreadyExists() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(ex, mockRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User Already Exists", response.getBody().getError());
        assertEquals("User already exists", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex, mockRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User Not Found", response.getBody().getError());
    }

    @Test
    void testHandleSummonerNotFound() {
        SummonerNotFoundException ex = new SummonerNotFoundException("Summoner not found");
        
        ResponseEntity<ErrorResponse> response = handler.handleSummonerNotFound(ex, mockRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Summoner Not Found", response.getBody().getError());
    }

    @Test
    void testHandleInvalidCredentials_WithBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex, mockRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid Credentials", response.getBody().getError());
    }

    @Test
    void testHandleInvalidCredentials_WithInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex, mockRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testHandleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Auth failed") {};
        
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex, mockRequest);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Authentication Failed", response.getBody().getError());
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, mockRequest);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().getError());
    }

    @Test
    void testHandleAuthorizationDeniedException() {
        AuthorizationDeniedException ex = mock(AuthorizationDeniedException.class);
        when(ex.getMessage()).thenReturn("Authorization denied");
        
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex, mockRequest);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().getError());
    }

    @Test
    void testHandleRiotApiException_NotFound() {
        RiotApiException ex = new RiotApiException("Summoner not found", 404);
        
        ResponseEntity<ErrorResponse> response = handler.handleRiotApiException(ex, mockRequest);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Riot API Error", response.getBody().getError());
    }

    @Test
    void testHandleRiotApiException_OtherError() {
        RiotApiException ex = new RiotApiException("Service unavailable", 503);
        
        ResponseEntity<ErrorResponse> response = handler.handleRiotApiException(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("Riot API Error", response.getBody().getError());
    }

    @Test
    void testHandleInvalidFile() {
        InvalidFileException ex = new InvalidFileException("Invalid file format");
        
        ResponseEntity<ErrorResponse> response = handler.handleInvalidFile(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid File", response.getBody().getError());
    }

    @Test
    void testHandleStorageException() {
        StorageException ex = new StorageException("Storage failed");
        
        ResponseEntity<ErrorResponse> response = handler.handleStorageException(ex, mockRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Storage Error", response.getBody().getError());
    }

    @Test
    void testHandleMaxUploadSizeExceeded() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1024);
        
        ResponseEntity<ErrorResponse> response = handler.handleMaxUploadSizeExceeded(ex, mockRequest);
        
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertEquals("File Too Large", response.getBody().getError());
    }

    @Test
    void testHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Request", response.getBody().getError());
    }

    @Test
    void testHandleIllegalState() {
        IllegalStateException ex = new IllegalStateException("Invalid state");
        
        ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, mockRequest);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "must not be blank");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));
        
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, mockRequest);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Validation Failed", response.getBody().get("error"));
        assertTrue(response.getBody().containsKey("validationErrors"));
    }

    @Test
    void testHandleGlobalException() {
        Exception ex = new Exception("Unexpected error");
        
        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, mockRequest);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    void testErrorResponse_Constructor() {
        ErrorResponse error = new ErrorResponse(404, "Not Found", "Resource not found", "/api/test");
        
        assertNotNull(error.getTimestamp());
        assertEquals(404, error.getStatus());
        assertEquals("Not Found", error.getError());
        assertEquals("Resource not found", error.getMessage());
        assertEquals("/api/test", error.getPath());
    }

    @Test
    void testErrorResponse_Setters() {
        ErrorResponse error = new ErrorResponse(200, "OK", "Success", "/api/test");
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        
        error.setTimestamp(newTime);
        error.setStatus(500);
        error.setError("Error");
        error.setMessage("Failed");
        error.setPath("/api/error");
        
        assertEquals(newTime, error.getTimestamp());
        assertEquals(500, error.getStatus());
        assertEquals("Error", error.getError());
        assertEquals("Failed", error.getMessage());
        assertEquals("/api/error", error.getPath());
    }

    @Test
    void testMultipleFieldValidationErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("user", "email", "must not be blank");
        FieldError error2 = new FieldError("user", "password", "must be at least 8 characters");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));
        
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, mockRequest);
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) body.get("validationErrors");
        assertEquals(2, validationErrors.size());
        assertTrue(validationErrors.containsKey("email"));
        assertTrue(validationErrors.containsKey("password"));
    }
}
