package org.domainmodule.post.repository;

import java.util.List;

import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

	List<PostImage> findAllByPost(Post post);
}
