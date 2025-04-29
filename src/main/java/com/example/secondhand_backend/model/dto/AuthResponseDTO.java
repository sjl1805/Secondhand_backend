package com.example.secondhand_backend.model.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer role;

    public AuthResponseDTO(String token, Long userId, String username, String nickname, String avatar, Integer role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.role = role;
    }
} 