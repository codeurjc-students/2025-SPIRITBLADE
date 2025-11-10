package com.tfg.tfg.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tfg.tfg.exception.UserAlreadyExistsException;
import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.model.entity.UserModel;
import com.tfg.tfg.repository.UserModelRepository;
import com.tfg.tfg.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserModelRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserModel testUser;
    private UserDTO testUserDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new UserModel();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPass("encodedPassword");
        testUser.setRols(Arrays.asList("USER"));
        testUser.setActive(true);

        testUserDTO = new UserDTO();
        testUserDTO.setName("newuser");
        testUserDTO.setEmail("new@example.com");
        testUserDTO.setPassword("password123");
        testUserDTO.setRoles(Arrays.asList("USER"));
        testUserDTO.setActive(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testFindByName() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.findByName("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getName());
        verify(userRepository).findByName("testuser");
    }

    @Test
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindFirstUser() {
        when(userRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.findFirstUser();

        assertTrue(result.isPresent());
        verify(userRepository).findFirstByOrderByIdAsc();
    }

    @Test
    void testFindAll() {
        Page<UserModel> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserModel> result = userService.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void testFindBySearch() {
        Page<UserModel> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("test", "test", pageable))
            .thenReturn(page);

        Page<UserModel> result = userService.findBySearch("test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase("test", "test", pageable);
    }

    @Test
    void testFindByRoleAndActive() {
        Page<UserModel> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findByRolsContainingAndActive("USER", true, pageable)).thenReturn(page);

        Page<UserModel> result = userService.findByRoleAndActive("USER", true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByRolsContainingAndActive("USER", true, pageable);
    }

    @Test
    void testFindByRole() {
        Page<UserModel> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findByRolsContaining("USER", pageable)).thenReturn(page);

        Page<UserModel> result = userService.findByRole("USER", pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByRolsContaining("USER", pageable);
    }

    @Test
    void testFindByActive() {
        Page<UserModel> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findByActive(true, pageable)).thenReturn(page);

        Page<UserModel> result = userService.findByActive(true, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByActive(true, pageable);
    }

    @Test
    void testCreateUser_Success() {
        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        UserModel result = userService.createUser(testUserDTO);

        assertNotNull(result);
        verify(userRepository).findByName("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testCreateUser_WithNullUserDTO() {
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(null));
    }

    @Test
    void testCreateUser_WithNullUsername() {
        testUserDTO.setName(null);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUserDTO));
    }

    @Test
    void testCreateUser_WithNullPassword() {
        testUserDTO.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUserDTO));
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        when(userRepository.findByName("newuser")).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(testUserDTO));
        verify(userRepository).findByName("newuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testCreateUser_WithDefaultRoles() {
        testUserDTO.setRoles(null);
        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        UserModel result = userService.createUser(testUserDTO);

        assertNotNull(result);
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testCreateUser_WithEmptyRoles() {
        testUserDTO.setRoles(new ArrayList<>());
        when(userRepository.findByName("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        UserModel result = userService.createUser(testUserDTO);

        assertNotNull(result);
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.updateUser(1L, testUserDTO);

        assertTrue(result.isPresent());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUpdateUser_WithNewPassword() {
        testUserDTO.setPassword("newPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.updateUser(1L, testUserDTO);

        assertTrue(result.isPresent());
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    void testUpdateUser_WithEmptyPassword() {
        testUserDTO.setPassword("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.updateUser(1L, testUserDTO);

        assertTrue(result.isPresent());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testUpdateUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.updateUser(1L, testUserDTO);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserProfile_Success() {
        testUserDTO.setAvatarUrl("http://example.com/avatar.png");
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.updateUserProfile("testuser", testUserDTO);

        assertTrue(result.isPresent());
        verify(userRepository).findByName("testuser");
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.updateUserProfile("testuser", testUserDTO);

        assertFalse(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testChangeUserRole_Success() {
        List<String> newRoles = Arrays.asList("ADMIN", "USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.changeUserRole(1L, newRoles);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testChangeUserRole_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.changeUserRole(1L, Arrays.asList("ADMIN"));

        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = userService.deleteUser(1L);

        assertFalse(result);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void testToggleUserActive_FromActiveToInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.toggleUserActive(1L);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testToggleUserActive_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.toggleUserActive(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testSetUserActive_ToActive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.setUserActive(1L, true);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testSetUserActive_ToInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.setUserActive(1L, false);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testSetUserActive_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.setUserActive(1L, true);

        assertFalse(result.isPresent());
    }

    @Test
    void testLinkSummoner_Success() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.linkSummoner("testuser", "puuid123", "summonerName", "EUW1");

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testLinkSummoner_NotFound() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.linkSummoner("testuser", "puuid123", "summonerName", "EUW1");

        assertFalse(result.isPresent());
    }

    @Test
    void testUnlinkSummoner_Success() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.unlinkSummoner("testuser");

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testUnlinkSummoner_NotFound() {
        when(userRepository.findByName("testuser")).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.unlinkSummoner("testuser");

        assertFalse(result.isPresent());
    }

    @Test
    void testFindAllUsers() {
        List<UserModel> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserModel> result = userService.findAllUsers();

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void testExistsById_True() {
        when(userRepository.existsById(1L)).thenReturn(true);

        boolean result = userService.existsById(1L);

        assertTrue(result);
        verify(userRepository).existsById(1L);
    }

    @Test
    void testExistsById_False() {
        when(userRepository.existsById(1L)).thenReturn(false);

        boolean result = userService.existsById(1L);

        assertFalse(result);
        verify(userRepository).existsById(1L);
    }

    @Test
    void testCountUsers() {
        when(userRepository.count()).thenReturn(5L);

        long result = userService.countUsers();

        assertEquals(5L, result);
        verify(userRepository).count();
    }

    @Test
    void testPromoteToAdmin_Success() {
        testUser.setRols(new ArrayList<>(Arrays.asList("USER")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.promoteToAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testPromoteToAdmin_AlreadyAdmin() {
        testUser.setRols(new ArrayList<>(Arrays.asList("USER", "ADMIN")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.promoteToAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testPromoteToAdmin_WithNullRoles() {
        testUser.setRols(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.promoteToAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testPromoteToAdmin_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.promoteToAdmin(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testDemoteFromAdmin_Success() {
        testUser.setRols(new ArrayList<>(Arrays.asList("USER", "ADMIN")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.demoteFromAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testDemoteFromAdmin_OnlyAdminRole() {
        testUser.setRols(new ArrayList<>(Arrays.asList("ADMIN")));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(testUser);

        Optional<UserModel> result = userService.demoteFromAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository).save(any(UserModel.class));
    }

    @Test
    void testDemoteFromAdmin_WithNullRoles() {
        testUser.setRols(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<UserModel> result = userService.demoteFromAdmin(1L);

        assertTrue(result.isPresent());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDemoteFromAdmin_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<UserModel> result = userService.demoteFromAdmin(1L);

        assertFalse(result.isPresent());
    }

    @Test
    void testSave() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        UserModel result = userService.save(testUser);

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepository).save(testUser);
    }
}
