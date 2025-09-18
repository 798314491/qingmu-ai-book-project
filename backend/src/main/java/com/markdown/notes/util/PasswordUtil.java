package com.markdown.notes.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password utility for generating BCrypt hashes
 */
public class PasswordUtil {
    
    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Generate password hashes
        String[] passwords = {"admin123", "123456", "password", "admin"};
        
        System.out.println("Password Hash Generator");
        System.out.println("======================");
        
        for (String password : passwords) {
            String hash = passwordEncoder.encode(password);
            System.out.println("Password: " + password);
            System.out.println("Hash: " + hash);
            System.out.println("Verify: " + passwordEncoder.matches(password, hash));
            System.out.println("---");
        }
        
        // Test the existing hash from database
        String existingHash = "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";
        System.out.println("Testing existing hash from database:");
        System.out.println("Hash: " + existingHash);
        
        for (String password : passwords) {
            boolean matches = passwordEncoder.matches(password, existingHash);
            System.out.println("Password '" + password + "' matches: " + matches);
        }
    }
}
