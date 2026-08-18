package com.cryptopulse.repository;

import com.cryptopulse.model.ProviderHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderHealthRepository extends JpaRepository<ProviderHealth, Long> {

    Optional<ProviderHealth> findByProviderName(String providerName);
}
