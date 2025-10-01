package com.tfg.tfg.unit;

import com.tfg.tfg.controller.LoginRestController;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.security.jwt.AuthResponse;
import com.tfg.tfg.security.jwt.LoginRequest;
import com.tfg.tfg.security.jwt.UserLoginService;
import com.tfg.tfg.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginRestControllerSimpleUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private LoginRestController loginRestController;

    @BeforeEach
    void setUp() {
        loginRestController = new LoginRestController(userService, userLoginService);
    }

    @Test
    void testLogin_Success() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password");
        AuthResponse expectedResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Login successful");
        ResponseEntity<AuthResponse> expectedResponseEntity = ResponseEntity.ok(expectedResponse);

        when(userLoginService.login(httpServletResponse, loginRequest)).thenReturn(expectedResponseEntity);

        // When
        ResponseEntity<AuthResponse> result = loginRestController.login(loginRequest, httpServletResponse);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(userLoginService).login(httpServletResponse, loginRequest);
    }

    @Test
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        AuthResponse expectedResponse = new AuthResponse(AuthResponse.Status.SUCCESS, "Token refreshed");
        ResponseEntity<AuthResponse> expectedResponseEntity = ResponseEntity.ok(expectedResponse);

        when(userLoginService.refresh(httpServletResponse, refreshToken)).thenReturn(expectedResponseEntity);

        // When
        ResponseEntity<AuthResponse> result = loginRestController.refreshToken(refreshToken, httpServletResponse);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(expectedResponse, result.getBody());
        verify(userLoginService).refresh(httpServletResponse, refreshToken);
    }

    @Test
    void testRefreshToken_WithNullToken() {
        // Given
        String refreshToken = null;
        AuthResponse expectedResponse = new AuthResponse(AuthResponse.Status.FAILURE, "Invalid token");
        ResponseEntity<AuthResponse> expectedResponseEntity = ResponseEntity.badRequest().body(expectedResponse);

        when(userLoginService.refresh(httpServletResponse, refreshToken)).thenReturn(expectedResponseEntity);

        // When
        ResponseEntity<AuthResponse> result = loginRestController.refreshToken(refreshToken, httpServletResponse);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        verify(userLoginService).refresh(httpServletResponse, refreshToken);
    }

    @Test
    void testLogout_Success() {
        // Given
        String logoutMessage = "Logout successful";
        when(userLoginService.logout(httpServletResponse)).thenReturn(logoutMessage);

        // When
        ResponseEntity<AuthResponse> result = loginRestController.logOut(httpServletResponse);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(AuthResponse.Status.SUCCESS, result.getBody().getStatus());
        assertEquals(logoutMessage, result.getBody().getMessage());
        verify(userLoginService).logout(httpServletResponse);
    }

    @Test
    void testRegister_Success() {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setName("testuser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");

        doNothing().when(userService).createUser(userDTO);

        // When
        ResponseEntity<Map<String, String>> result = loginRestController.register(userDTO);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User registered successfully", result.getBody().get("message"));
        verify(userService).createUser(userDTO);
    }

    @Test
    void testMe_WithAuthenticatedUser() {
        // Given
        String username = "testuser";

        doReturn(username).when(authentication).getName();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn("userPrincipal").when(authentication).getPrincipal();
        doReturn(Arrays.asList(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        )).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            ResponseEntity<Map<String, Object>> result = loginRestController.me();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(username, result.getBody().get("username"));
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) result.getBody().get("roles");
            assertNotNull(roles);
            assertEquals(2, roles.size());
            assertTrue(roles.contains("ROLE_USER"));
            assertTrue(roles.contains("ROLE_ADMIN"));
        }
    }

    @Test
    void testMe_WithUnauthenticatedUser() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            ResponseEntity<Map<String, Object>> result = loginRestController.me();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void testMe_WithNullAuthentication() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            ResponseEntity<Map<String, Object>> result = loginRestController.me();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void testMe_WithAnonymousUser() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            ResponseEntity<Map<String, Object>> result = loginRestController.me();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertNull(result.getBody());
        }
    }

    @Test
    void testMe_WithEmptyRoles() {
        // Given
        String username = "testuser";

        doReturn(username).when(authentication).getName();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn("userPrincipal").when(authentication).getPrincipal();
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            ResponseEntity<Map<String, Object>> result = loginRestController.me();

            // Then
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(username, result.getBody().get("username"));
            
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) result.getBody().get("roles");
            assertNotNull(roles);
            assertTrue(roles.isEmpty());
        }
    }
}