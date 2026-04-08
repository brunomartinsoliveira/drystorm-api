package com.drystorm.api.service;

import com.drystorm.api.dto.request.LoginRequest;
import com.drystorm.api.dto.response.AuthResponse;
import com.drystorm.api.entity.User;
import com.drystorm.api.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);

        return AuthResponse.of(token, user.getEmail(), user.getName(), user.getRole().name());
    }
}
