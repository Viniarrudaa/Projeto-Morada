package br.com.morada.dto;

public record AuthResponse(
    String token,
    UserResponse user
) {
}
