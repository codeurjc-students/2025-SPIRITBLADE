package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tfg.tfg.model.dto.UserDTO;
import com.tfg.tfg.security.jwt.AuthResponse;
import com.tfg.tfg.security.jwt.AuthResponse.Status;
import com.tfg.tfg.security.jwt.LoginRequest;
import com.tfg.tfg.security.jwt.UserLoginService;
import com.tfg.tfg.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Authentication", description = "Authentication management endpoints")
@RestController
@RequestMapping("/api/v1/auth")
public class LoginRestController {

	private final UserService userService;
    private final UserLoginService userLoginService;

    public LoginRestController(UserService userService, UserLoginService userLoginService) {
        this.userService = userService;
        this.userLoginService = userLoginService;
    }

	@Operation(
		summary = "User login",
		description = "Authenticates a user with username and password, returns JWT token"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Login successful",
			content = @Content(schema = @Schema(implementation = AuthResponse.class))
		),
		@ApiResponse(
			responseCode = "401",
			description = "Invalid credentials"
		)
	})
	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		
		return userLoginService.login(loginRequest);
	}

	@Operation(
		summary = "User logout",
		description = "Logs out the current user and invalidates the refresh token"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "Logout successful",
			content = @Content(schema = @Schema(implementation = AuthResponse.class))
		)
	})
	@PostMapping("/logout")
	public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
	}

	@Operation(
		summary = "User registration",
		description = "Registers a new user in the system"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "201",
			description = "User registered successfully"
		),
		@ApiResponse(
			responseCode = "400",
			description = "Invalid user data or username already exists"
		)
	})
	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO userDTO) {

		userService.createUser(userDTO);

		Map<String, String> response = new HashMap<>();
		response.put("message", "User registered successfully");

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(
		summary = "Get current user info",
		description = "Returns basic information about the currently authenticated user"
	)
	@ApiResponses(value = {
		@ApiResponse(
			responseCode = "200",
			description = "User information retrieved successfully"
		),
		@ApiResponse(
			responseCode = "401",
			description = "User not authenticated"
		)
	})
	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> me() {
		var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		// Build a simple user info response with username and roles
		Map<String, Object> resp = new HashMap<>();
		String username = auth.getName();
		resp.put("username", username);
		var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
		resp.put("roles", roles);

		return ResponseEntity.ok(resp);
	}
}
