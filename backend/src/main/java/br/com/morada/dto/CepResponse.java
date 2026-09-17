package br.com.morada.dto;

public record CepResponse(
    String cep,
    String street,
    String district,
    String city,
    String state
) {
}
