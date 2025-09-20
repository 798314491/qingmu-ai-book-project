package com.markdown.notes.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.notes.dto.JwtResponse;
import com.markdown.notes.dto.LoginRequest;
import com.markdown.notes.dto.RegisterRequest;
import com.markdown.notes.entity.User;
import com.markdown.notes.mapper.UserMapper;
import com.markdown.notes.security.JwtUtils;
import com.markdown.notes.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Authentication Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Value("${jwt.expiration}")
    private int jwtExpiration;
    
    @Transactional
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication;
        UserPrincipal userPrincipal;
        
        // 特殊处理：如果用户名是"test"，跳过密码验证
        if ("test".equals(loginRequest.getUsername())) {
            log.info("Test user login detected, skipping password validation");
            
            // 查找或创建test用户
            User testUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", "test"));
            if (testUser == null) {
                // 创建test用户
                testUser = new User();
                testUser.setUsername("hello");
                testUser.setEmail("admin@admin.com");
                testUser.setPassword(passwordEncoder.encode("123456"));
                testUser.setNickname("Test User");
                testUser.setStatus(1);
                testUser.setCreatedAt(LocalDateTime.now());
                testUser.setUpdatedAt(LocalDateTime.now());
                userMapper.insert(testUser);
                log.info("Created test user with ID: {}", testUser.getId());
            }
            
            // 创建UserPrincipal
            userPrincipal = UserPrincipal.create(testUser);
            
            // 创建认证对象
            authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities());
        } else {
            // 正常认证流程
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            userPrincipal = (UserPrincipal) authentication.getPrincipal();
        }
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(loginRequest.getUsername());
        
        // Update last login time
        User user = userMapper.selectById(userPrincipal.getId());
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
        
        // Store refresh token in Redis
        String redisKey = "refresh_token:" + userPrincipal.getId();
        redisTemplate.opsForValue().set(redisKey, refreshToken, 7, TimeUnit.DAYS);
        
        return JwtResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn((long) jwtExpiration)
                .userId(userPrincipal.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }
    
    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        // Check if username exists
        if (userMapper.exists(new QueryWrapper<User>().eq("username", registerRequest.getUsername()))) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email exists
        if (userMapper.exists(new QueryWrapper<User>().eq("email", registerRequest.getEmail()))) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setNickname(registerRequest.getNickname() != null ? 
                registerRequest.getNickname() : registerRequest.getUsername());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // Auto login after registration
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());
        
        return login(loginRequest);
    }
    
    public JwtResponse refreshToken(String refreshToken) {
        if (jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUsernameFromJwtToken(refreshToken);
            String newAccessToken = jwtUtils.generateTokenFromUsername(username);
            
            User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
            
            return JwtResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn((long) jwtExpiration)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .build();
        }
        
        throw new RuntimeException("Invalid refresh token");
    }
    
    public void logout(String token) {
        // Extract token without "Bearer " prefix
        String jwt = token.substring(7);
        String username = jwtUtils.getUsernameFromJwtToken(jwt);
        
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user != null) {
            // Remove refresh token from Redis
            String redisKey = "refresh_token:" + user.getId();
            redisTemplate.delete(redisKey);
            
            // Add access token to blacklist
            String blacklistKey = "blacklist:" + jwt;
            redisTemplate.opsForValue().set(blacklistKey, "true", jwtExpiration, TimeUnit.MILLISECONDS);
        }
        
        SecurityContextHolder.clearContext();
    }
}