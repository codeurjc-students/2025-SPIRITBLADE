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
	public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.setAllowedOriginPatterns(List.of("*")); // Allow all origins (including OCI public IPs)
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setMaxAge(3600L);
		source.registerCorsConfiguration("/**", config);
		return source;
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
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		http.securityMatcher("/api/v1/**")
				.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt)
						.accessDeniedHandler(customAccessDeniedHandler));

		http.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/summoners/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

				.requestMatchers("/api/v1/dashboard/**").authenticated()
				.requestMatchers("/api/v1/users/**").authenticated()
				.requestMatchers("/api/v1/admin/**").authenticated()
				.anyRequest().authenticated());

		http.formLogin(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);

		http.csrf(csrf -> csrf.disable());

		http.httpBasic(org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer::disable);

		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}