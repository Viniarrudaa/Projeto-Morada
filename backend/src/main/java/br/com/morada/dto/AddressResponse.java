package br.com.morada.dto;

public record AddressResponse(
    Long id,
    Long userId,
    String cep,
    String number,
    String complement,
    String street,
    String district,
    String city,
    String state,
    boolean primaryAddress
) {
}
