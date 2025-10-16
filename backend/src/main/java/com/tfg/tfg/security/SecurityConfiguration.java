package com.tfg.tfg.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
		config.addAllowedOrigin("http://localhost:4200");
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
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
					// Allow unauthenticated POSTs to auth endpoints (login/register)
					.requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
					// Allow unauthenticated GETs for summoner lookup
					.requestMatchers(HttpMethod.GET, "/api/v1/summoners/**").permitAll()
					// Allow public access to file serving endpoints (avatars, images, etc.)
					.requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
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