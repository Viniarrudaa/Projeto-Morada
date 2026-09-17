package br.com.morada.repository;

import br.com.morada.domain.CepCache;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CepCacheRepository extends JpaRepository<CepCache, String> {
}
