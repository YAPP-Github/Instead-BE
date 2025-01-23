package org.domainmodule.agent.repository;

import java.util.Optional;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlatform;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, Long> {
	Optional<Agent> findByAccountIdAndPlatform(String accountId, AgentPlatform platform);
}
