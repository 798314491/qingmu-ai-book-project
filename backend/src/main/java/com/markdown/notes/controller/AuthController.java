package com.markdown.notes.controller;

import com.markdown.notes.common.Result;
import com.markdown.notes.dto.LoginRequest;
import com.markdown.notes.dto.RegisterRequest;
import com.markdown.notes.dto.JwtResponse;
import com.markdown.notes.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication APIs")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User login")
    public Result<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return Result.success(authService.login(loginRequest));
    }
    
    @PostMapping("/register")
    @Operation(summary = "User registration")
    public Result<JwtResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return Result.success(authService.register(registerRequest));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token")
    public Result<JwtResponse> refreshToken(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }
}