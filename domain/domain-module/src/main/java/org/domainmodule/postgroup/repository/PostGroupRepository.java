package org.domainmodule.postgroup.repository;

import java.util.Optional;

import org.domainmodule.postgroup.entity.PostGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostGroupRepository extends JpaRepository<PostGroup, Long> {

	@Query("""
			select pg from PostGroup pg
			join fetch pg.agent
			join fetch pg.agent.user
			where pg.agent.user.id = :userId
			and pg.agent.id = :agentId
			and pg.id = :postGroupId
		""")
	Optional<PostGroup> findByUserIdAndAgentIdAndId(Long userId, Long agentId, Long postGroupId);
}
