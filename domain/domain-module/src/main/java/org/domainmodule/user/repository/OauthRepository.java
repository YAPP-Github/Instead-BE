package org.domainmodule.user.repository;

import java.util.Optional;

import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
	Optional<Oauth> findByProviderAndProviderId(ProviderType provider, String providerId);
}
