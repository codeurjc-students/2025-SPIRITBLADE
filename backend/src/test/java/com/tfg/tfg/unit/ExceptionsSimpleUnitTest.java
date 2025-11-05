package com.tfg.tfg.unit;

import com.tfg.tfg.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsSimpleUnitTest {

    @Test
    void testStorageException_WithMessage() {
        StorageException exception = new StorageException("Storage error");
        
        assertEquals("Storage error", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testStorageException_WithMessageAndCause() {
        Throwable cause = new RuntimeException("Root cause");
        StorageException exception = new StorageException("Storage error", cause);
        
        assertEquals("Storage error", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals(cause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }

    @Test
    void testInvalidFileException() {
        InvalidFileException exception = new InvalidFileException("Invalid file format");
        
        assertEquals("Invalid file format", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testRiotApiException_WithStatusCode() {
        RiotApiException exception = new RiotApiException("API error", 404);
        
        assertEquals("API error", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void testRiotApiException_DifferentStatusCodes() {
        RiotApiException exception400 = new RiotApiException("Bad Request", 400);
        assertEquals(400, exception400.getStatusCode());
        
        RiotApiException exception401 = new RiotApiException("Unauthorized", 401);
        assertEquals(401, exception401.getStatusCode());
        
        RiotApiException exception403 = new RiotApiException("Forbidden", 403);
        assertEquals(403, exception403.getStatusCode());
        
        RiotApiException exception429 = new RiotApiException("Rate Limited", 429);
        assertEquals(429, exception429.getStatusCode());
        
        RiotApiException exception500 = new RiotApiException("Server Error", 500);
        assertEquals(500, exception500.getStatusCode());
        
        RiotApiException exception503 = new RiotApiException("Service Unavailable", 503);
        assertEquals(503, exception503.getStatusCode());
    }

    @Test
    void testUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("User not found");
        
        assertEquals("User not found", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testUserAlreadyExistsException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("User exists");
        
        assertEquals("User exists", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testSummonerNotFoundException() {
        SummonerNotFoundException exception = new SummonerNotFoundException("Summoner not found");
        
        assertEquals("Summoner not found", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException("Invalid credentials");
        
        assertEquals("Invalid credentials", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionsCanBeThrown() {
        assertThrows(StorageException.class, () -> {
            throw new StorageException("Test");
        });
        
        assertThrows(InvalidFileException.class, () -> {
            throw new InvalidFileException("Test");
        });
        
        assertThrows(RiotApiException.class, () -> {
            throw new RiotApiException("Test", 500);
        });
        
        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException("Test");
        });
        
        assertThrows(UserAlreadyExistsException.class, () -> {
            throw new UserAlreadyExistsException("Test");
        });
        
        assertThrows(SummonerNotFoundException.class, () -> {
            throw new SummonerNotFoundException("Test");
        });
        
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException("Test");
        });
    }

    @Test
    void testExceptionsCanBeCaught() {
        try {
            throw new StorageException("Test exception");
        } catch (StorageException e) {
            assertEquals("Test exception", e.getMessage());
        }
        
        try {
            throw new RiotApiException("API failed", 503);
        } catch (RiotApiException e) {
            assertEquals("API failed", e.getMessage());
            assertEquals(503, e.getStatusCode());
        }
    }
}
