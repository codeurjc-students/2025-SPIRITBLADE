package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.tfg.tfg.security.jwt.AuthResponse;
import com.tfg.tfg.security.jwt.JwtTokenProvider;
import com.tfg.tfg.security.jwt.LoginRequest;
import com.tfg.tfg.security.jwt.UserLoginService;

import io.jsonwebtoken.Claims;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private Authentication authentication;

    @Mock
    private Claims claims;

    private UserLoginService userLoginService;

    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        userLoginService = new UserLoginService(authenticationManager, userDetailsService, jwtTokenProvider);
        testUserDetails = User.builder()
                .username("testuser")
                .password("encoded-password")
                .roles("USER")
                .build();
    }

    @Test
    void testLoginSuccess() {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(testUserDetails);
        when(jwtTokenProvider.generateAccessToken(testUserDetails)).thenReturn("access-token-123");
        when(jwtTokenProvider.generateRefreshToken(testUserDetails)).thenReturn("refresh-token-456");

        ResponseEntity<AuthResponse> response = userLoginService.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthResponse.Status.SUCCESS, response.getBody().getStatus());
        assertEquals("Auth successful", response.getBody().getMessage());
        assertEquals("access-token-123", response.getBody().getAccessToken());
        assertEquals("refresh-token-456", response.getBody().getRefreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("testuser");
        verify(jwtTokenProvider).generateAccessToken(testUserDetails);
        verify(jwtTokenProvider).generateRefreshToken(testUserDetails);
    }

    @Test
    void testRefreshFailure() {

        String invalidRefreshToken = "invalid-or-expired-token";
        when(jwtTokenProvider.validateToken(invalidRefreshToken))
                .thenThrow(new RuntimeException("Token expired"));

        ResponseEntity<AuthResponse> response = userLoginService.refresh(httpServletResponse, invalidRefreshToken);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(AuthResponse.Status.FAILURE, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Invalid or expired"));
        verify(httpServletResponse, never()).addCookie(any());
    }

    @Test
    void testLogout() {

        String result = userLoginService.logout(httpServletResponse);

        assertEquals("logout successfully", result);

        verify(httpServletResponse, times(2)).addCookie(any());
    }
}
