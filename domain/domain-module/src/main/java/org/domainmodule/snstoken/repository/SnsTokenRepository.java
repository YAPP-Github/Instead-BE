package org.domainmodule.snstoken.repository;

import java.util.Optional;

import org.domainmodule.snstoken.entity.SnsToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnsTokenRepository extends JpaRepository<SnsToken, Long> {
	Optional<SnsToken> findByAgentId(long agentId);
}
