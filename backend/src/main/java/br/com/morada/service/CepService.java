package br.com.morada.service;

import br.com.morada.domain.CepCache;
import br.com.morada.dto.CepResponse;
import br.com.morada.exception.ApiException;
import br.com.morada.repository.CepCacheRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class CepService {

    private final CepCacheRepository cepCacheRepository;
    private final RestClient restClient;

    public CepService(CepCacheRepository cepCacheRepository, @Value("${app.viacep.base-url}") String viacepBaseUrl) {
        this.cepCacheRepository = cepCacheRepository;
        this.restClient = RestClient.create(viacepBaseUrl);
    }

    @Transactional
    public CepResponse findAddress(String cep) {
        String normalizedCep = normalizeCep(cep);

        return cepCacheRepository.findById(normalizedCep)
            .map(this::toResponse)
            .orElseGet(() -> fetchAndCache(normalizedCep));
    }

    public String normalizeCep(String value) {
        String cep = value == null ? "" : value.replaceAll("\\D", "");
        if (cep.length() != 8) {
            throw ApiException.badRequest("invalid_cep", "CEP deve conter 8 dígitos.");
        }
        return cep;
    }

    private CepResponse fetchAndCache(String cep) {
        try {
            ViaCepResponse response = restClient.get()
                .uri("/{cep}/json", cep)
                .retrieve()
                .body(ViaCepResponse.class);

            if (response == null || Boolean.TRUE.equals(response.erro())) {
                throw ApiException.notFound("cep_not_found", "CEP não encontrado.");
            }

            CepCache cache = new CepCache();
            cache.setCep(cep);
            cache.setStreet(orDefault(response.logradouro()));
            cache.setDistrict(orDefault(response.bairro()));
            cache.setCity(orDefault(response.localidade()));
            cache.setState(orDefault(response.uf()).toUpperCase());
            cache.setUpdatedAt(Instant.now());

            cepCacheRepository.save(cache);
            return toResponse(cache);
        } catch (ApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw ApiException.badRequest("viacep_unavailable", "Não foi possível consultar o ViaCEP agora.");
        }
    }

    private CepResponse toResponse(CepCache cache) {
        return new CepResponse(
            cache.getCep(),
            cache.getStreet(),
            cache.getDistrict(),
            cache.getCity(),
            cache.getState()
        );
    }

    private String orDefault(String value) {
        if (value == null || value.isBlank()) {
            return "Não informado";
        }
        return value.trim();
    }

    private record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        String localidade,
        String uf,
        Boolean erro
    ) {
    }
}
