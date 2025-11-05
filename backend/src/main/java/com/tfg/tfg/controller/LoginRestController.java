package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginRestController {

	private final UserService userService;
    private final UserLoginService userLoginService;

    public LoginRestController(UserService userService, UserLoginService userLoginService) {
        this.userService = userService;
        this.userLoginService = userLoginService;
    }

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		
		return userLoginService.login(loginRequest);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(
			@CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {

		return userLoginService.refresh(response, refreshToken);
	}

	@PostMapping("/logout")
	public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO userDTO) {

		userService.createUser(userDTO);

		Map<String, String> response = new HashMap<>();
		response.put("message", "User registered successfully");

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

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
