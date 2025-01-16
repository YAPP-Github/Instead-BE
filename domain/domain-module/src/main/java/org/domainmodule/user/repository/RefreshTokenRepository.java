package org.domainmodule.user.repository;

import java.util.Optional;

import org.domainmodule.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByUserId(Long userId);
}
