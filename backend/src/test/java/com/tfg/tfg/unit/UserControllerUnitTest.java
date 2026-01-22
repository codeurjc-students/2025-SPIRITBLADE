package com.tfg.tfg.unit;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.RiotService;
import com.tfg.tfg.service.UserAvatarService;
import com.tfg.tfg.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @Mock
    private RiotService riotService;

    @Mock
    private UserAvatarService userAvatarService;

    @Mock
    private SecurityContext securityContext;

    private UserController controller;

    private UserModel testUser;
    private UserModel testUserInactive;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService, riotService, userAvatarService);

        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setActive(true);
        testUser.setRols(List.of("ROLE_USER"));

        testUserInactive = new UserModel();
        testUserInactive.setId(2L);
        testUserInactive.setName("inactiveuser");
        testUserInactive.setEmail("inactive@example.com");
        testUserInactive.setActive(false);
        testUserInactive.setRols(List.of("ROLE_USER"));

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetMyProfileUserFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Object> response = controller.getMyProfile();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserDTO);
        UserDTO userDTO = (UserDTO) response.getBody();
        assertEquals("testuser", userDTO.getName());
        verify(userService, times(2)).findByName("testuser");
    }

    @Test
    void testGetMyProfileUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "nonexistent", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = controller.getMyProfile();

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(2)).findByName("nonexistent");
    }

    @Test
    void testGetMyProfileUserDeactivated() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "inactiveuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("inactiveuser")).thenReturn(Optional.of(testUserInactive));

        // Act
        ResponseEntity<Object> response = controller.getMyProfile();

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(false, body.get("success"));
        assertEquals("User account is deactivated", body.get("message"));
        verify(userService).findByName("inactiveuser");
    }

    @Test
    void testUploadAvatarSuccess() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "avatar.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userAvatarService.uploadAvatar("testuser", file)).thenReturn("http://avatar.url");

        // Act
        ResponseEntity<Object> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("http://avatar.url", body.get("avatarUrl"));
        verify(userAvatarService).uploadAvatar("testuser", file);
    }

    @Test
    void testUploadAvatarUserNotFound() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "avatar.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "nonexistent", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userAvatarService.uploadAvatar(eq("nonexistent"), any())).thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            controller.uploadAvatar(file);
        });
        
        verify(userAvatarService).uploadAvatar(eq("nonexistent"), any());
    }

    @Test
    void testUploadAvatarUserDeactivated() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "avatar.png", 
            "image/png", 
            "test image content".getBytes()
        );
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "inactiveuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("inactiveuser")).thenReturn(Optional.of(testUserInactive));

        // Act
        ResponseEntity<Object> response = controller.uploadAvatar(file);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(false, body.get("success"));
        assertEquals("User account is deactivated", body.get("message"));
        verify(userService).findByName("inactiveuser");
        verify(userAvatarService, never()).uploadAvatar(any(), any());
    }

    @Test
    void testUpdateMyProfileSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        
        UserDTO updateDto = new UserDTO();
        updateDto.setEmail("newemail@example.com");
        
        UserModel updatedUser = new UserModel();
        updatedUser.setId(1L);
        updatedUser.setName("testuser");
        updatedUser.setEmail("newemail@example.com");
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userService.updateUserProfile(eq("testuser"), any(UserDTO.class))).thenReturn(Optional.of(updatedUser));

        // Act
        ResponseEntity<Object> response = controller.updateMyProfile(updateDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO resultDto = (UserDTO) response.getBody();
        assertEquals("newemail@example.com", resultDto.getEmail());
    }

    @Test
    void testLinkSummonerSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        
        Map<String, String> request = new HashMap<>();
        request.put("summonerName", "TestSummoner#EUW");
        request.put("region", "EUW");
        
        SummonerDTO summonerDto = new SummonerDTO();
        summonerDto.setName("TestSummoner#EUW");
        summonerDto.setPuuid("test-puuid");
        summonerDto.setLevel(100);
        summonerDto.setProfileIconId(1);
        
        when(riotService.getSummonerByName("TestSummoner#EUW")).thenReturn(summonerDto);
        when(userService.linkSummoner("testuser", "test-puuid", "TestSummoner#EUW", "EUW")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Object> response = controller.linkSummoner(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testUnlinkSummonerSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlinkSummoner("testuser")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Object> response = controller.unlinkSummoner();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testGetLinkedSummonerSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        
        UserModel linkedUser = new UserModel();
        linkedUser.setName("testuser");
        linkedUser.setLinkedSummonerPuuid("test-puuid");
        linkedUser.setLinkedSummonerName("TestSummoner#EUW");
        linkedUser.setLinkedSummonerRegion("EUW");
        linkedUser.setActive(true);
        
        when(userService.findByName("testuser")).thenReturn(Optional.of(linkedUser));

        // Act
        ResponseEntity<Object> response = controller.getLinkedSummoner();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("linked"));
        assertEquals("TestSummoner#EUW", body.get("summonerName"));
    }

    @Test
    void testDeleteAvatarSuccess() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(userAvatarService).deleteAvatar("testuser");

        // Act
        ResponseEntity<Object> response = controller.deleteAvatar();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userAvatarService).deleteAvatar("testuser");
    }

    @Test
    void testUpdateMyProfileUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser)); // Active check passes
        
        UserDTO updateDto = new UserDTO();
        when(userService.updateUserProfile(eq("testuser"), any(UserDTO.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = controller.updateMyProfile(updateDto);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLinkSummonerUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser)); // Active check passes
        
        Map<String, String> request = new HashMap<>();
        request.put("summonerName", "TestSummoner#EUW");
        request.put("region", "EUW");
        
        SummonerDTO summonerDto = new SummonerDTO();
        summonerDto.setName("TestSummoner#EUW");
        summonerDto.setPuuid("test-puuid");
        
        when(riotService.getSummonerByName("TestSummoner#EUW")).thenReturn(summonerDto);
        when(userService.linkSummoner("testuser", "test-puuid", "TestSummoner#EUW", "EUW")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = controller.linkSummoner(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode()); // Controller catches exception and returns BAD_REQUEST
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(((String)body.get("message")).contains("User not found"));
    }

    @Test
    void testUnlinkSummonerUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser)); // Active check passes
        when(userService.unlinkSummoner("testuser")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = controller.unlinkSummoner();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(((String)body.get("message")).contains("User not found"));
    }

    @Test
    void testGetLinkedSummonerUserNotFound() {
        // Arrange
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        // First call for active check succeeds
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser))
                                                .thenReturn(Optional.empty()); // Second call fails

        // Act
        ResponseEntity<Object> response = controller.getLinkedSummoner();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
