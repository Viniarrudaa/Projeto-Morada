package br.com.morada.repository;

import br.com.morada.domain.User;
import br.com.morada.domain.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCpf(String cpf);

    Optional<User> findFirstByRole(UserRole role);

    boolean existsByCpf(String cpf);
}
