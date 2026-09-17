package br.com.morada.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
    @NotBlank String cep,
    @NotBlank String number,
    String complement,
    boolean primaryAddress
) {
}
