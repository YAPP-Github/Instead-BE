package org.mainapplication.global.config;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.security.config.Customizer.*;

import org.mainapplication.global.constants.UrlConstants;
import org.mainapplication.global.constants.WebSecurityURI;
import org.mainapplication.global.filter.JwtAuthenticationFilter;
import org.mainapplication.global.filter.TestAuthenticationFilter;
import org.mainapplication.global.oauth2.handler.CustomAuthenticationEntryPoint;
import org.mainapplication.global.oauth2.handler.CustomOAuth2FailureHandler;
import org.mainapplication.global.oauth2.handler.CustomOAuth2SuccessHandler;
import org.mainapplication.global.oauth2.service.CustomOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
	// private final JwtAuthenticationFilter jwtAuthenticaltionFilter;
	private final CustomOauth2UserService customOauth2UserService;
	private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
	private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final TestAuthenticationFilter testAuthenticationFilter;

	private void defaultBasicFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.anonymous(AbstractHttpConfigurer::disable)
			.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		defaultBasicFilterChain(http);

		//http.requiresChannel(rcc -> rcc.anyRequest().requiresSecure()); //https만 요청가능
		http.authorizeHttpRequests(
				authorize ->
					authorize
						.requestMatchers("/","/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**")
						.permitAll()
						.requestMatchers(WebSecurityURI.PUBLIC_URIS.toArray(String[]::new))
						.permitAll()
						.anyRequest()
						.authenticated()
			)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOauth2UserService)
				)
				.successHandler(customOAuth2SuccessHandler)
				.failureHandler(customOAuth2FailureHandler)
			)
			.exceptionHandling(exceptionHandling ->
				exceptionHandling
					.authenticationEntryPoint(customAuthenticationEntryPoint.oAuth2EntryPoint())
			)
			.addFilterBefore(testAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
			// .addFilterBefore(jwtAuthenticaltionFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(WebSecurityURI.CORS_ALLOW_URIS);
		configuration.addAllowedOriginPattern(UrlConstants.LOCAL_DOMAIN_URL);
		configuration.addAllowedOriginPattern(UrlConstants.PROD_DOMAIN_URL);
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);
		configuration.addExposedHeader(SET_COOKIE);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public static PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
