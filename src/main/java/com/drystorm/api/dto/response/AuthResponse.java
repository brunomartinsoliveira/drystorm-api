package com.drystorm.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private String email;
    private String name;
    private String role;

    public static AuthResponse of(String token, String email, String name, String role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(email)
                .name(name)
                .role(role)
                .build();
    }
}
