package com.markdown.notes.security;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.markdown.notes.entity.User;
import com.markdown.notes.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom User Details Service
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("username", username)
                .or()
                .eq("email", username));
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username or email: " + username);
        }
        
        return UserPrincipal.create(user);
    }
    
    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userMapper.selectById(id);
        
        if (user == null) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
        
        return UserPrincipal.create(user);
    }
}