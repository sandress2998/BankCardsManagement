package com.example.bankcards.service;

import com.example.bankcards.dto.AdminRequest;
import com.example.bankcards.dto.JwtResponse;
import com.example.bankcards.dto.UserInfoResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.impl.UserServiceImpl;
import com.example.bankcards.util.BCryptEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock JwtService jwtService;
    @Mock Authentication authentication;
    @Mock SecurityContext securityContext;
    MockEnvironment env = new MockEnvironment().withProperty("security.admin.secret", "hashed_admin_secret");

    UserServiceImpl service;

    String validLogin = "alex";
    String longLogin = "a".repeat(User.LOGIN_MAX_LENGTH + 1);

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        service = new UserServiceImpl(env, userRepository, jwtService);
    }

    // findByLogin
    @Test
    void findByLogin_returnsUser_whenExists() {
        User user = new User(validLogin, "encryptedPassword", User.Role.USER);
        when(userRepository.findByLogin(validLogin)).thenReturn(user);
        assertEquals(user, service.findByLogin(validLogin));
    }

    @Test
    void findByLogin_throwsNotFoundException_whenNotFound() {
        when(userRepository.findByLogin(validLogin)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.findByLogin(validLogin));
    }

    @Test
    void findByLogin_throwsIllegalArgument_whenLoginTooLong() {
        assertThrows(IllegalArgumentException.class, () -> service.findByLogin(longLogin));
    }

    // checkIfNotExistsByLogin
    @Test
    void checkIfNotExistsByLogin_throwsBadRequest_whenUserExists() {
        when(userRepository.existsByLogin(validLogin)).thenReturn(true);
        assertThrows(BadRequestException.class, () -> service.checkIfNotExistsByLogin(validLogin));
    }

    @Test
    void checkIfNotExistsByLogin_ok_whenUserNotExists() {
        when(userRepository.existsByLogin(validLogin)).thenReturn(false);
        assertDoesNotThrow(() -> service.checkIfNotExistsByLogin(validLogin));
    }

    @Test
    void checkIfNotExistsByLogin_throwsIllegalArgument_whenLoginTooLong() {
        assertThrows(IllegalArgumentException.class, () -> service.checkIfNotExistsByLogin(longLogin));
    }

    // save
    @Test
    void save_returnsSavedUser() {
        User user = new User(validLogin, "encryptedPassword", User.Role.USER);
        when(userRepository.save(user)).thenReturn(user);
        assertEquals(user, service.save(user));
    }

    @Test
    void save_throwsIllegalArgument_whenLoginTooLong() {
        User user = new User(longLogin, "encryptedPassword", User.Role.USER);
        assertThrows(IllegalArgumentException.class, () -> service.save(user));
    }

    // requestAdmin
    @Test
    void requestAdmin_setsAdminRole_andReturnsJwt_onCorrectSecret() {
        UUID userId = UUID.randomUUID();
        AdminRequest req = new AdminRequest("supersecret");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(authentication.getCredentials()).thenReturn("jwt");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.existsById(userId)).thenReturn(true);
        try (MockedStatic<BCryptEncoder> encMock = mockStatic(BCryptEncoder.class)) {
            encMock.when(() -> BCryptEncoder.matches("supersecret", "hashed_admin_secret")).thenReturn(true);
            when(jwtService.changeRoleInJwt("jwt", User.Role.ADMIN)).thenReturn("new_jwt");
            JwtResponse resp = service.requestAdmin(req);
            assertEquals("new_jwt", resp.jwt());
            verify(userRepository).updateRole(eq(userId), eq(User.Role.ADMIN));
        }
    }

    @Test
    void requestAdmin_throwsAccessDenied_onWrongSecret() {
        UUID userId = UUID.randomUUID();
        AdminRequest req = new AdminRequest("badsecret");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId.toString());
        when(authentication.getCredentials()).thenReturn("jwt");
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.existsById(userId)).thenReturn(true);
        try (MockedStatic<BCryptEncoder> encMock = mockStatic(BCryptEncoder.class)) {
            encMock.when(() -> BCryptEncoder.matches("badsecret", "hashed_admin_secret")).thenReturn(false);
            assertThrows(AccessDeniedException.class, () -> service.requestAdmin(req));
        }
    }

    @Test
    void requestAdmin_throwsUnauthorized_ifNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        AdminRequest req = new AdminRequest("anysecret");
        assertThrows(UnauthorizedException.class, () -> service.requestAdmin(req));
    }

    // getAll
    @Test
    void getAll_returnsUserList() {
        User user1 = new User(validLogin, "encryptedPassword1", User.Role.USER);
        User user2 = new User("dima", "encryptedPassword2", User.Role.ADMIN);
        Page<User> users = new PageImpl<>(List.of(user1, user2));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        List<UserInfoResponse> result = service.getAll(0, 2);
        assertEquals(2, result.size());
        assertEquals(validLogin, result.get(0).login());
        assertEquals("dima", result.get(1).login());
    }

    @Test
    void getAll_returnsEmptyList_whenNoUsers() {
        Page<User> users = new PageImpl<>(List.of());
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        List<UserInfoResponse> result = service.getAll(0, 1);
        assertTrue(result.isEmpty());
    }

    // getUserInfoById
    @Test
    void getUserInfoById_returnsUserInfo_whenFound() {
        UUID userId = UUID.randomUUID();
        User user = new User(validLogin, "encryptedPassword", User.Role.ADMIN);
        user.setId(userId);
        when(userRepository.findUserById(userId)).thenReturn(user);
        UserInfoResponse resp = service.getUserInfoById(userId);
        assertEquals(userId, resp.id());
        assertEquals(validLogin, resp.login());
        assertEquals(User.Role.ADMIN, resp.role());
    }

    @Test
    void getUserInfoById_throwsNotFound_whenUserAbsent() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findUserById(userId)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.getUserInfoById(userId));
    }
}
