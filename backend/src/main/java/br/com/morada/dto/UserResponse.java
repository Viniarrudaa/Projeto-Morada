package br.com.morada.dto;

import br.com.morada.domain.UserRole;
import java.time.LocalDate;

public record UserResponse(
    Long id,
    String name,
    String cpf,
    LocalDate birthDate,
    UserRole role
) {
}
