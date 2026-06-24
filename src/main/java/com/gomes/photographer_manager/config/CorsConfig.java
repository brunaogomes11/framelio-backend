package com.gomes.photographer_manager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private static final List<String> METHODS =
			List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");

	private final List<String> allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins:http://localhost:5173}") String origins) {
		this.allowedOrigins = Arrays.stream(origins.split(","))
				.map(String::trim)
				.filter(o -> !o.isBlank())
				.toList();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(METHODS);
		config.setAllowCredentials(true);
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Content-Disposition"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins(allowedOrigins.toArray(String[]::new))
				.allowedMethods(METHODS.toArray(String[]::new))
				.allowCredentials(true)
				.allowedHeaders("*")
				.exposedHeaders("Content-Disposition");
	}
}
