package org.domainmodule.sns.repository;

import org.domainmodule.sns.entity.SnsProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnsProviderRespository extends JpaRepository<SnsProvider, Long> {
	boolean existsByClientIdAndClientSecret(String clientId, String clientSecret);
}
