package br.com.morada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank String name,
    @NotBlank String cpf,
    @NotNull @Past LocalDate birthDate,
    @NotBlank @Size(min = 6) String password
) {
}
