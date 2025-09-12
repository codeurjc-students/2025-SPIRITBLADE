package com.tfg.tfg.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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

	@Autowired
	private UserService userService;

    @Autowired
	private UserLoginService userLoginService;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		
		return userLoginService.login(response, loginRequest);
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

	@PostMapping("/user")
	public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {

		userService.createUser(userDTO);

		Map<String, String> response = new HashMap<>();
		response.put("message", "User registered successfully");

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/register")
	public ResponseEntity<?> registerAlias(@RequestBody UserDTO userDTO) {
		// Alias for frontend expecting /auth/register
		userService.createUser(userDTO);
		Map<String, String> response = new HashMap<>();
		response.put("message", "User registered successfully");
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
