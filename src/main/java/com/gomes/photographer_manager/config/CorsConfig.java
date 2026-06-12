package com.gomes.photographer_manager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOriginPatterns("*")
				.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD", "TRACE", "CONNECT")
				.allowCredentials(true) // Permitir credenciais (cookies)
				.allowedHeaders("*") // Permitir todos os cabeçalhos
				.exposedHeaders("Content-Disposition"); // Expor cabeçalhos personalizados
		;
	}
}
