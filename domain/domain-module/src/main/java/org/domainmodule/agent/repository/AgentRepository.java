package org.domainmodule.agent.repository;

import java.util.List;
import java.util.Optional;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlatformType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AgentRepository extends JpaRepository<Agent, Long> {

	Optional<Agent> findByAccountIdAndPlatform(String accountId, AgentPlatformType platform);

	@Query("""
			select a from Agent a
			join fetch a.user
			where a.user.id = :userId
		""")
	List<Agent> findAllByUserId(Long userId);
}
