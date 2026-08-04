package com.aiworkspace.impl;

import com.aiworkspace.dto.LoginRequest;
import com.aiworkspace.dto.LoginResponse;
import com.aiworkspace.dto.RegisterRequest;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.entity.User;
import com.aiworkspace.exception.DuplicateResourceException;
import com.aiworkspace.exception.UnauthorizedException;
import com.aiworkspace.repository.UserRepository;
import com.aiworkspace.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        jwtService = mock(JwtService.class);
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void getAllUserReturnsMappedUsersFromRepository() {
        User first = user(1L, "John", "Doe", "john@example.com", "encoded-1", "USER");
        User second = user(2L, "Jane", "Doe", "jane@example.com", "encoded-2", "ADMIN");
        when(userRepository.findAll()).thenReturn(List.of(first, second));

        List<UserResponse> result = userService.getAllUser();

        assertEquals(2, result.size());
        assertEquals("john@example.com", result.get(0).getEmail());
        assertEquals("USER", result.get(0).getRole());
        assertEquals("jane@example.com", result.get(1).getEmail());
        assertEquals("ADMIN", result.get(1).getRole());
    }

    @Test
    void getAllUsersReturnsMappedPageForRequestedPageable() {
        Pageable pageable = PageRequest.of(1, 2);
        Page<User> page = new PageImpl<>(
                List.of(user(3L, "Alex", "Ray", "alex@example.com", "encoded", "USER")),
                pageable,
                5
        );
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getNumberOfElements());
        assertEquals(5, result.getTotalElements());
        assertEquals("alex@example.com", result.getContent().get(0).getEmail());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void registerCreatesUserWithEncodedPasswordAndDefaultRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("plain-password");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User toSave = invocation.getArgument(0);
            toSave.setId(10L);
            return toSave;
        });

        UserResponse result = userService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedCandidate = userCaptor.getValue();

        assertEquals("John", savedCandidate.getFirstName());
        assertEquals("Doe", savedCandidate.getLastName());
        assertEquals("john@example.com", savedCandidate.getEmail());
        assertEquals("encoded-password", savedCandidate.getPassword());
        assertEquals("USER", savedCandidate.getRole());
        assertNotNull(savedCandidate.getCreatedAt());
        assertNotNull(savedCandidate.getUpdatedAt());
        assertEquals(10L, result.getId());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@example.com");
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.register(request)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loginReturnsTokenAndUserWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("plain-password");

        User storedUser = user(7L, "John", "Doe", "john@example.com", "encoded-password", "USER");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("john@example.com")).thenReturn("jwt-token");

        LoginResponse result = userService.login(request);

        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUser());
        assertEquals(7L, result.getUser().getId());
        assertEquals("john@example.com", result.getUser().getEmail());
    }

    @Test
    void loginThrowsWhenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("plain-password");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid Email or Password", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong-password");

        User storedUser = user(7L, "John", "Doe", "john@example.com", "encoded-password", "USER");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> userService.login(request)
        );

        assertEquals("Invalid Email or Password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void searchUsersReturnsMappedResultsForKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> repositoryPage = new PageImpl<>(
                List.of(user(11L, "Alice", "Smith", "alice@example.com", "encoded", "USER")),
                pageable,
                1
        );
        when(userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        eq("alice"),
                        eq("alice"),
                        eq("alice"),
                        eq(pageable)
                ))
                .thenReturn(repositoryPage);

        Page<UserResponse> result = userService.searchUsers("alice", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("alice@example.com", result.getContent().get(0).getEmail());
        verify(userRepository)
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        "alice",
                        "alice",
                        "alice",
                        pageable
                );
    }

    @Test
    void searchUsersReturnsEmptyPageWhenNoMatchesFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = Page.empty(pageable);
        when(userRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        eq("nomatch"),
                        eq("nomatch"),
                        eq("nomatch"),
                        eq(pageable)
                ))
                .thenReturn(emptyPage);

        Page<UserResponse> result = userService.searchUsers("nomatch", pageable);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    private User user(Long id, String firstName, String lastName, String email, String password, String role) {
        return User.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .role(role)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
