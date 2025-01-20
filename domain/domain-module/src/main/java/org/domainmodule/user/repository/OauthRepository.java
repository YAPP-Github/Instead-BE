package org.domainmodule.user.repository;

import java.util.Optional;

import org.domainmodule.user.entity.Oauth;
import org.domainmodule.user.entity.type.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OauthRepository extends JpaRepository<Oauth, Long> {
	@Query("SELECT o FROM Oauth o JOIN FETCH o.user WHERE o.provider = :provider AND o.providerId = :providerId")
	Optional<Oauth> findByProviderAndProviderId(@Param("provider") ProviderType provider, @Param("providerId") String providerId);
}
