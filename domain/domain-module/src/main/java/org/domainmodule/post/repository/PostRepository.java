package org.domainmodule.post.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.domainmodule.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {
	@Query("SELECT post FROM Post post WHERE post.uploadTime BETWEEN :startTime AND :endTime")
	List<Post> findPostsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

	@Query("""
    SELECT p FROM Post p 
    JOIN FETCH p.postGroup pg
    JOIN FETCH pg.agent a
    JOIN SnsToken t ON t.agent.id = a.id
    WHERE p.uploadTime BETWEEN :startTime AND :endTime
""")
	List<Post> findPostsWithSnsTokenByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}
