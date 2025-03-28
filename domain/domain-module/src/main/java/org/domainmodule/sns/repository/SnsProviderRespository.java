package org.domainmodule.sns.repository;

import java.util.Optional;

import org.domainmodule.sns.entity.SnsProvider;
import org.domainmodule.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnsProviderRespository extends JpaRepository<SnsProvider, Long> {
	Optional<SnsProvider> findByUserAndClientIdAndClientSecret(User user, String clientId, String clientSecret);
}
