package org.domainmodule.agent.repository;

import java.util.Optional;

import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AgentPersonalSettingRepository extends JpaRepository<AgentPersonalSetting, Long> {

	@Query("""
			select aps from AgentPersonalSetting aps
			join fetch aps.agent
			join fetch aps.agent.user
			where aps.agent.id = :agentId
			and aps.agent.user.id = :userId
		""")
	Optional<AgentPersonalSetting> findByUserIdAndAgentId(Long userId, Long agentId);
}
