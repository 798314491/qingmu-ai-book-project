package com.markdown.notes;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Markdown Notes Application Main Class
 */
@SpringBootApplication
@MapperScan("com.markdown.notes.mapper")
@EnableCaching
@EnableAsync
@EnableScheduling
public class MarkdownNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarkdownNotesApplication.class, args);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Markdown Notes System Started!         ║");
        System.out.println("║   http://localhost:8080                  ║");
        System.out.println("║   Swagger: http://localhost:8080/api/swagger-ui.html ║");
        System.out.println("╚══════════════════════════════════════════╝");
    }
}