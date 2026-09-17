package br.com.morada.security;

import br.com.morada.domain.UserRole;

public record AuthenticatedUser(
    Long id,
    String cpf,
    UserRole role
) {
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
