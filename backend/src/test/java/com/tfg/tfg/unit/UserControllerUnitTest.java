package com.tfg.tfg.unit;

import com.tfg.tfg.controller.UserController;
import com.tfg.tfg.model.dto.SummonerDTO;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.service.interfaces.IRiotService;
import com.tfg.tfg.service.interfaces.IUserAvatarService;
import com.tfg.tfg.service.interfaces.IUserService;
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
    private IUserService userService;

    @Mock
    private IRiotService riotService;

    @Mock
    private IUserAvatarService userAvatarService;

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

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<Object> response = controller.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof UserDTO);
        UserDTO userDTO = (UserDTO) response.getBody();
        assertEquals("testuser", userDTO.getName());
        verify(userService, times(2)).findByName("testuser");
    }

    @Test
    void testGetMyProfileUserNotFound() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "nonexistent", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.getMyProfile();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService, times(2)).findByName("nonexistent");
    }

    @Test
    void testGetMyProfileUserDeactivated() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "inactiveuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("inactiveuser")).thenReturn(Optional.of(testUserInactive));

        ResponseEntity<Object> response = controller.getMyProfile();

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

        ResponseEntity<Object> response = controller.uploadAvatar(file);

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

        assertThrows(RuntimeException.class, () -> {
            controller.uploadAvatar(file);
        });
        
        verify(userAvatarService).uploadAvatar(eq("nonexistent"), any());
    }

    @Test
    void testUploadAvatarUserDeactivated() {

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

        ResponseEntity<Object> response = controller.uploadAvatar(file);

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

        ResponseEntity<Object> response = controller.updateMyProfile(updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO resultDto = (UserDTO) response.getBody();
        assertEquals("newemail@example.com", resultDto.getEmail());
    }

    @Test
    void testLinkSummonerSuccess() {

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

        ResponseEntity<Object> response = controller.linkSummoner(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testUnlinkSummonerSuccess() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlinkSummoner("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<Object> response = controller.unlinkSummoner();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
    }

    @Test
    void testGetLinkedSummonerSuccess() {

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

        ResponseEntity<Object> response = controller.getLinkedSummoner();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("linked"));
        assertEquals("TestSummoner#EUW", body.get("summonerName"));
    }

    @Test
    void testDeleteAvatarSuccess() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        doNothing().when(userAvatarService).deleteAvatar("testuser");

        ResponseEntity<Object> response = controller.deleteAvatar();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userAvatarService).deleteAvatar("testuser");
    }

    @Test
    void testUpdateMyProfileUserNotFound() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        
        UserDTO updateDto = new UserDTO();
        when(userService.updateUserProfile(eq("testuser"), any(UserDTO.class))).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.updateMyProfile(updateDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testLinkSummonerUserNotFound() {

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
        
        when(riotService.getSummonerByName("TestSummoner#EUW")).thenReturn(summonerDto);
        when(userService.linkSummoner("testuser", "test-puuid", "TestSummoner#EUW", "EUW")).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.linkSummoner(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(((String)body.get("message")).contains("User not found"));
    }

    @Test
    void testUnlinkSummonerUserNotFound() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userService.unlinkSummoner("testuser")).thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.unlinkSummoner();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue(((String)body.get("message")).contains("User not found"));
    }

    @Test
    void testGetLinkedSummonerUserNotFound() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            "testuser", null, Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);

        when(userService.findByName("testuser")).thenReturn(Optional.of(testUser))
                                                .thenReturn(Optional.empty());

        ResponseEntity<Object> response = controller.getLinkedSummoner();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testGetMyProfilenullAuthenticationreturnsFirstUser() {

        when(securityContext.getAuthentication()).thenReturn(null);
        UserModel firstUser = new UserModel();
        firstUser.setName("admin");
        firstUser.setActive(true);
        when(userService.findFirstUser()).thenReturn(Optional.of(firstUser));

        ResponseEntity<Object> response = controller.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).findFirstUser();
    }

    @Test
    void testGetMyProfilewithImageFallbackWhenAvatarUrlNull() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, java.util.Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        UserModel userWithImage = new UserModel();
        userWithImage.setName("testuser");
        userWithImage.setActive(true);
        userWithImage.setImage("http://legacy.image.url");
        userWithImage.setAvatarUrl(null);
        when(userService.findByName("testuser")).thenReturn(Optional.of(userWithImage));

        ResponseEntity<Object> response = controller.getMyProfile();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserDTO dto = (UserDTO) response.getBody();
        assertNotNull(dto);
        assertEquals("http://legacy.image.url", dto.getAvatarUrl());
    }

    @Test
    void testGetLinkedSummonernotLinkedreturnsLinkedFalse() {

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, java.util.Collections.emptyList()
        );
        when(securityContext.getAuthentication()).thenReturn(auth);
        UserModel noLinkUser = new UserModel();
        noLinkUser.setName("testuser");
        noLinkUser.setActive(true);
        noLinkUser.setLinkedSummonerPuuid(null);
        when(userService.findByName("testuser")).thenReturn(Optional.of(noLinkUser));

        ResponseEntity<Object> response = controller.getLinkedSummoner();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) response.getBody();
        assertEquals(false, body.get("linked"));
    }

}
