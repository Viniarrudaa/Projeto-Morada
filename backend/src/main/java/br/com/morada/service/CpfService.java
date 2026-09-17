package br.com.morada.service;

import br.com.morada.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public class CpfService {

    public String normalizeAndValidate(String value) {
        String cpf = onlyDigits(value);
        if (!isValid(cpf)) {
            throw ApiException.badRequest("invalid_cpf", "CPF inválido.");
        }
        return cpf;
    }

    public String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    public boolean isValid(String cpf) {
        if (cpf == null || cpf.length() != 11 || cpf.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(cpf.substring(0, 9), 10);
        int secondDigit = calculateDigit(cpf.substring(0, 9) + firstDigit, 11);

        return cpf.equals(cpf.substring(0, 9) + firstDigit + secondDigit);
    }

    private int calculateDigit(String base, int weight) {
        int sum = 0;
        for (char digit : base.toCharArray()) {
            sum += Character.getNumericValue(digit) * weight;
            weight--;
        }

        int result = 11 - (sum % 11);
        return result >= 10 ? 0 : result;
    }
}
