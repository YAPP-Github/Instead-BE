package org.domainmodule.post.repository;

import java.util.List;

import org.domainmodule.post.entity.PromptHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromptHistoryRepository extends JpaRepository<PromptHistory, Long> {
	@Query("""
	select ph from PromptHistory ph
	join fetch ph.post p
	join fetch p.postGroup pg
	join fetch pg.agent a
	join fetch a.user u
	where u.id = :userId
	and a.id = :agentId
	and pg.id = :postGroupId
	and p.id = :postId
	""")
	List<PromptHistory> findPromptHistoriesWithValidation(
		@Param("userId") Long userId,
		@Param("agentId") Long agentId,
		@Param("postGroupId") Long postGroupId,
		@Param("postId") Long postId
	);
}
