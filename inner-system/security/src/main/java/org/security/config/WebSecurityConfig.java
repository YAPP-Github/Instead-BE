package org.security.config;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.security.config.Customizer.*;

import org.security.constants.UrlConstants;
import org.security.constants.WebSecurityURI;
import org.security.jwt.JwtAuthenticationFilter;
import org.security.oauth2.CustomOAuth2SuccessHandler;
import org.security.oauth2.CustomOauth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticaltionFilter;
	private final CustomOauth2UserService customOauth2UserService;
	private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

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
						.requestMatchers("/")
						.permitAll()
						.requestMatchers(WebSecurityURI.PUBLIC_URIS.toArray(new String[0]))
						.permitAll()
						.anyRequest()
						.permitAll()
			)
			.addFilterBefore(jwtAuthenticaltionFilter, UsernamePasswordAuthenticationFilter.class)
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOauth2UserService) // OAuth2UserService 추가
				)
				.successHandler(customOAuth2SuccessHandler)
			);
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(WebSecurityURI.CORS_ALLOW_URIS);
		configuration.addAllowedOriginPattern(UrlConstants.LOCAL_DOMAIN_URL.getValue());
		configuration.addAllowedOriginPattern(UrlConstants.LOCAL_SECURE_DOMAIN_URL.getValue());
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
