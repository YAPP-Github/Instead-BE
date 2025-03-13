package org.domainmodule.post.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.postgroup.entity.PostGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

	// post와 SnsToken을 한번에 조회
	@Query("""
		    SELECT p FROM Post p
		    JOIN FETCH p.postGroup pg
		    JOIN FETCH pg.agent a
		    JOIN SnsToken t ON t.agent.id = a.id
		    WHERE p.uploadTime BETWEEN :startTime AND :endTime
		    And p.status = :status
		""")
	List<Post> findPostsWithSnsTokenByTimeRange(LocalDateTime startTime, LocalDateTime endTime,
		@Param("status") PostStatusType status);

	// Agent와 postIds에 해당하는 Post 리스트를 조회
	@Query("""
			select p from Post p
			where p.postGroup.agent = :agent
			and p.id in :postIds
		""")
	List<Post> findAllByAgentAndId(Agent agent, List<Long> postIds);

	// PostGroup과 postIds에 해당하는 Post 리스트를 조회
	@Query("""
			select p from Post p
			where p.postGroup = :postGroup
			and p.id in :postIds
		""")
	List<Post> findAllByPostGroupAndId(PostGroup postGroup, List<Long> postIds);

	// PostGroup에 해당하는 Post 리스트를 조회
	@Query("""
			select distinct p from Post p
			where p.postGroup = :postGroup
		""")
	List<Post> findAllByPostGroup(PostGroup postGroup);

	// PostGroup에 해당하는 Post 리스트를 이미지까지 함께 조회
	@Query("""
			select distinct p from Post p
			left join fetch p.postImages
			where p.postGroup = :postGroup
		""")
	List<Post> findAllWithImagesByPostGroup(PostGroup postGroup);

	// PostGroup과 postId에 해당하는 Post를 조회
	@Query("""
			select p from Post p
			where p.postGroup = :postGroup
			and p.id = :postId
		""")
	Optional<Post> findByPostGroupAndId(PostGroup postGroup, Long postId);

	// PostGroup과 postId에 해당하는 Post를 이미지까지 함께 조회
	@Query("""
			select distinct p from Post p
			left join fetch p.postImages
			where p.postGroup = :postGroup
			and p.id = :postId
		""")
	Optional<Post> findWithImagesByPostGroupAndId(PostGroup postGroup, Long postId);

	// PostGroup에 해당하는 Post 중에서, 상태가 GENERATED인 게시물 중 order가 가장 큰 Post 조회
	@Query("""
		    select p from Post p
		    where p.postGroup = :postGroup
		    and p.status = :status
		    order by p.displayOrder desc
		    limit 1
		""")
	Optional<Post> findLastGeneratedPost(PostGroup postGroup, PostStatusType status);

	// Agent 게시글 중, 특정 status를 가진 모든 Post를 uploadTime 오름차순으로 조회
	@Query("""
		    SELECT p FROM Post p
		    JOIN p.postGroup pg
		    JOIN pg.agent a
		    WHERE a.user.id = :userId
		    AND a.id = :agentId
		    AND p.status = :status
		    ORDER BY p.uploadTime
		""")
	List<Post> findAllReservedPostsByUserAndAgent(
		@Param("userId") Long userId,
		@Param("agentId") Long agentId,
		@Param("status") PostStatusType status);

	// user, agent, postgroup, 특정 status를 가진 post를 displayOrder 오름차순 순서로 조회
	@Query("""
    SELECT p FROM Post p
    JOIN p.postGroup pg
    JOIN pg.agent a
    WHERE a.user.id = :userId
    AND a.id = :agentId
    AND p.status = :status
    AND pg.id = :postGroupId
    ORDER BY p.displayOrder ASC
    """)
	List<Post> findPostsByUserAndAgentAndStatus(
		@Param("userId") Long userId,
		@Param("agentId") Long agentId,
		@Param("postGroupId") Long postGroupId,
		@Param("status") PostStatusType status);
}
