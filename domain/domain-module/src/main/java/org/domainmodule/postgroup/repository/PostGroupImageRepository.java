package org.domainmodule.postgroup.repository;

import java.util.List;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostGroupImageRepository extends JpaRepository<PostGroupImage, Long> {

	List<PostGroupImage> findAllByPostGroup(PostGroup postGroup);
}
