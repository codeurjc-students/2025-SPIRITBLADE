package com.tfg.tfg.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.tfg.tfg.security.jwt.JwtRequestFilter;
import com.tfg.tfg.security.jwt.UnauthorizedHandlerJwt;
import com.tfg.tfg.security.jwt.CustomAccessDeniedHandler;
import com.tfg.tfg.service.RepositoryUserDetailsService;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;



@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	private final JwtRequestFilter jwtRequestFilter;
	private final RepositoryUserDetailsService userDetailsService;
	private final UnauthorizedHandlerJwt unauthorizedHandlerJwt;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	public SecurityConfiguration(JwtRequestFilter jwtRequestFilter,
								RepositoryUserDetailsService userDetailsService,
								UnauthorizedHandlerJwt unauthorizedHandlerJwt,
								CustomAccessDeniedHandler customAccessDeniedHandler) {
		this.jwtRequestFilter = jwtRequestFilter;
		this.userDetailsService = userDetailsService;
		this.unauthorizedHandlerJwt = unauthorizedHandlerJwt;
		this.customAccessDeniedHandler = customAccessDeniedHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		// Allow both HTTP and HTTPS from frontend
		config.addAllowedOrigin("http://localhost:4200");
		config.addAllowedOrigin("https://localhost:4200");
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.addAllowedHeader("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		
		http.authenticationProvider(authenticationProvider());

		// CORS is handled by the CorsFilter bean defined above, so we can use default configuration
		http.cors(cors -> {});
		
		http
			.securityMatcher("/api/v1/**")
			.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt).accessDeniedHandler(customAccessDeniedHandler));
		
		http
			.authorizeHttpRequests(authorize -> authorize
					// Allow preflight requests
					.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					
					// ========== PUBLIC ENDPOINTS (Home & Summoner pages) ==========
					// Authentication endpoints (login/register)
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
					// All GET summoner endpoints - public League of Legends data
					// Used by: Home component (recent searches) & Summoner component (all queries)
					.requestMatchers(HttpMethod.GET, "/api/v1/summoners/**").permitAll()
					// File serving endpoints - public access to avatars and images
					.requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
					// Swagger UI and OpenAPI documentation
					.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
					
					// ========== PROTECTED ENDPOINTS (Dashboard & Admin) ==========
					// Dashboard endpoints - require authentication, user can only access their own data
					// Backend services verify user ownership
					.requestMatchers("/api/v1/dashboard/**").authenticated()
					// User management endpoints - require authentication
					.requestMatchers("/api/v1/users/**").authenticated()
					// Admin panel - require authentication and ADMIN role
					// Role-based authorization is handled by @PreAuthorize annotations in controller
					.requestMatchers("/api/v1/admin/**").authenticated()
					
					// Any other request to /api/v1/** requires authentication
					.anyRequest().authenticated()
			);
		
        // Disable Form login Authentication
        http.formLogin(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}