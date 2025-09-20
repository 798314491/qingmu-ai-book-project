package com.markdown.notes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration - 单独的CORS配置类，便于管理
 */
@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:80,http://localhost,http://127.0.0.1:80,http://127.0.0.1}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH}")
    private String allowedMethods;
    
    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Value("${cors.max-age:3600}")
    private long maxAge;
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 设置允许的来源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        
        // 在开发环境允许所有源（可通过环境变量控制）
        String profile = System.getProperty("spring.profiles.active", "dev");
        if ("dev".equals(profile) || "docker".equals(profile)) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        }
        
        // 设置允许的方法
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // 设置允许的头部
        configuration.setAllowedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization", 
            "Access-Control-Request-Method", "Access-Control-Request-Headers",
            "X-Requested-With", "X-Auth-Token", "Cache-Control"
        ));
        
        // 设置暴露的头部
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials",
            "Authorization", "Content-Type", "X-Total-Count"
        ));
        
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
