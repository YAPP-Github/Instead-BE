package org.domainmodule.agent.repository;

import java.util.List;
import java.util.Optional;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.type.AgentPlatformType;
import org.domainmodule.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentRepository extends JpaRepository<Agent, Long> {

	Optional<Agent> findByUserAndAccountIdAndPlatform(User user, String accountId, AgentPlatformType platform);

	@Query("""
			select a from Agent a
			join fetch a.user
			where a.user.id = :userId
		""")
	List<Agent> findAllByUserId(Long userId);

	@Query("""
			select a from Agent a
			join fetch a.user
			where a.user.id = :userId
			and a.id = :agentId
		""")
	Optional<Agent> findByUserIdAndId(Long userId, Long agentId);
}
