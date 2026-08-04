package com.aiworkspace.controller;

import com.aiworkspace.common.ApiResponse;
import com.aiworkspace.dto.UserResponse;
import com.aiworkspace.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UserControllerTest {

	private UserService userService;
	private UserController userController;

	@BeforeEach
	void setUp() {
		userService = mock(UserService.class);
		userController = new UserController(userService);
	}

	@Test
	void getAllUserReturnsSuccessfulResponseWithUsers() {
		List<UserResponse> users = List.of(
				new UserResponse(1L, "John", "Doe", "john@example.com", "USER"),
				new UserResponse(2L, "Jane", "Doe", "jane@example.com", "ADMIN")
		);
		when(userService.getAllUser()).thenReturn(users);

		ResponseEntity<ApiResponse<List<UserResponse>>> response = userController.getAllUser();

		assertEquals(200, response.getStatusCode().value());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isSuccess());
		assertEquals("Users fetched successfully", response.getBody().getMessage());
		assertEquals(users, response.getBody().getData());
		assertNotNull(response.getBody().getTimestamp());
		assertFalse(response.getBody().getTimestamp().isBlank());
	}

	@Test
	void getAllUsersByPageReturnsPagedUsersForRequestedPageAndSize() {
		Page<UserResponse> pageResult = new PageImpl<>(
				List.of(new UserResponse(1L, "John", "Doe", "john@example.com", "USER")),
				PageRequest.of(1, 2),
				5
		);
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResult);

		ResponseEntity<ApiResponse<Page<UserResponse>>> response = userController.getAllUsersByPage(1, 2);

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).getAllUsers(pageableCaptor.capture());
		Pageable captured = pageableCaptor.getValue();

		assertEquals(1, captured.getPageNumber());
		assertEquals(2, captured.getPageSize());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isSuccess());
		assertEquals(pageResult, response.getBody().getData());
	}

	@Test
	void getUsersBySortingAppliesDescendingSortWhenDirectionIsDesc() {
		Page<UserResponse> pageResult = new PageImpl<>(List.of());
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResult);

		ResponseEntity<ApiResponse<Page<UserResponse>>> response =
				userController.getUsersBySorting(0, 10, "email", "desc");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).getAllUsers(pageableCaptor.capture());
		Pageable captured = pageableCaptor.getValue();

		assertNotNull(captured.getSort().getOrderFor("email"));
		assertTrue(captured.getSort().getOrderFor("email").isDescending());
		assertNotNull(response.getBody());
		assertEquals(pageResult, response.getBody().getData());
	}

	@Test
	void getUsersBySortingFallsBackToAscendingWhenDirectionIsUnknown() {
		Page<UserResponse> pageResult = new PageImpl<>(List.of());
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResult);

		userController.getUsersBySorting(0, 10, "id", "invalid-direction");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).getAllUsers(pageableCaptor.capture());
		Pageable captured = pageableCaptor.getValue();

		assertNotNull(captured.getSort().getOrderFor("id"));
		assertTrue(captured.getSort().getOrderFor("id").isAscending());
	}

	@Test
	void getUsersReturnsAllUsersWhenKeywordIsNull() {
		Page<UserResponse> pageResult = new PageImpl<>(List.of());
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResult);

		ResponseEntity<ApiResponse<Page<UserResponse>>> response =
				userController.getUsers(null, 0, 10, "id", "asc");

		verify(userService).getAllUsers(any(Pageable.class));
		verify(userService, never()).searchUsers(any(), any(Pageable.class));
		assertNotNull(response.getBody());
		assertEquals(pageResult, response.getBody().getData());
	}

	@Test
	void getUsersReturnsAllUsersWhenKeywordIsBlank() {
		Page<UserResponse> pageResult = new PageImpl<>(List.of());
		when(userService.getAllUsers(any(Pageable.class))).thenReturn(pageResult);

		userController.getUsers("   ", 0, 10, "id", "asc");

		verify(userService).getAllUsers(any(Pageable.class));
		verify(userService, never()).searchUsers(any(), any(Pageable.class));
	}

	@Test
	void getUsersSearchesUsersWhenKeywordProvided() {
		Page<UserResponse> pageResult = new PageImpl<>(List.of(
				new UserResponse(3L, "Alice", "Smith", "alice@example.com", "USER")
		));
		when(userService.searchUsers(eq("alice"), any(Pageable.class))).thenReturn(pageResult);

		ResponseEntity<ApiResponse<Page<UserResponse>>> response =
				userController.getUsers("alice", 2, 5, "email", "desc");

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(userService).searchUsers(eq("alice"), pageableCaptor.capture());
		verify(userService, never()).getAllUsers(any(Pageable.class));

		Pageable captured = pageableCaptor.getValue();
		assertEquals(2, captured.getPageNumber());
		assertEquals(5, captured.getPageSize());
		assertNotNull(captured.getSort().getOrderFor("email"));
		assertTrue(captured.getSort().getOrderFor("email").isDescending());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().isSuccess());
		assertEquals(pageResult, response.getBody().getData());
	}

}