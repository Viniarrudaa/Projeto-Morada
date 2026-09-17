package br.com.morada.repository;

import br.com.morada.domain.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserIdOrderByPrimaryAddressDescIdAsc(Long userId);

    Optional<Address> findFirstByUserIdOrderByIdAsc(Long userId);

    long countByUserId(Long userId);

    @Modifying
    @Query("update Address a set a.primaryAddress = false where a.user.id = :userId")
    void clearPrimaryForUser(@Param("userId") Long userId);
}
