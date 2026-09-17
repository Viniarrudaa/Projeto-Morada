package br.com.morada.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.morada.exception.ApiException;
import org.junit.jupiter.api.Test;

class CpfServiceTest {

    private final CpfService cpfService = new CpfService();

    @Test
    void acceptsValidFormattedCpfAndStoresOnlyDigits() {
        String cpf = cpfService.normalizeAndValidate("181.149.677-65");

        assertThat(cpf).isEqualTo("18114967765");
    }

    @Test
    void rejectsCpfWithInvalidCheckDigits() {
        assertThatThrownBy(() -> cpfService.normalizeAndValidate("529.982.247-24"))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("CPF inválido");
    }

    @Test
    void rejectsCpfWithAllEqualDigits() {
        assertThat(cpfService.isValid("11111111111")).isFalse();
    }
}
