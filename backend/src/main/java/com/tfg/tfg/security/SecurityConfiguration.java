package com.tfg.tfg.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	public RepositoryUserDetailsService userDetailsService;

	@Autowired
	private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

	@Autowired
	private CustomAccessDeniedHandler customAccessDeniedHandler;

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
		
		http
			.securityMatcher("/api/**")
			.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt).accessDeniedHandler(customAccessDeniedHandler));
		
		http
			.authorizeHttpRequests(authorize -> authorize
                    // PUBLIC ENDPOINTS
                    .requestMatchers(HttpMethod.GET,"/api/v1/users/*").permitAll()
					.requestMatchers(HttpMethod.GET,"/api/v1/users/*/image").permitAll()
					.requestMatchers(HttpMethod.GET,"/api/v1/products/*").permitAll()
					.requestMatchers(HttpMethod.GET,"/api/v1/products").permitAll()
					.requestMatchers(HttpMethod.GET,"/api/v1/products/*/image").permitAll()
					.requestMatchers(HttpMethod.GET,"/api/v1/products/*/offers").permitAll()

					.requestMatchers(HttpMethod.POST,"/api/v1/auth/*").permitAll()

					// PRIVATE ENDPOINTS
					.requestMatchers(HttpMethod.GET,"/api/v1/users").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.GET,"/api/v1/users/*/products").hasAnyRole("USER")
					.requestMatchers(HttpMethod.GET,"/api/v1/users/*/boughtProducts").hasAnyRole("USER")
					
					.requestMatchers(HttpMethod.POST,"/api/v1/products").hasAnyRole("USER")
					.requestMatchers(HttpMethod.POST,"/api/v1/products/*/ratings").hasAnyRole("USER")
					.requestMatchers(HttpMethod.POST,"/api/v1/products/*/offers").hasAnyRole("USER")
					.requestMatchers(HttpMethod.POST,"/api/v1/products/*/image").hasAnyRole("USER")

					.requestMatchers(HttpMethod.PUT,"/api/v1/products/*").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT,"/api/v1/users").hasAnyRole("USER")
					.requestMatchers(HttpMethod.PUT,"/api/v1/users/*/active").hasAnyRole("ADMIN")
					.requestMatchers(HttpMethod.PUT,"/api/v1/products/*/image").hasAnyRole("USER", "ADMIN")
					.requestMatchers(HttpMethod.PUT,"/api/v1/users/*/image").hasAnyRole("USER", "ADMIN")
					
					.requestMatchers(HttpMethod.DELETE,"/api/v1/products/*").hasAnyRole("USER", "ADMIN")
			);
		
        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}