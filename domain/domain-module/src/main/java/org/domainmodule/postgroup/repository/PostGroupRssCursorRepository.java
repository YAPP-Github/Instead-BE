package org.domainmodule.postgroup.repository;

import java.util.Optional;

import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.entity.PostGroupRssCursor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostGroupRssCursorRepository extends JpaRepository<PostGroupRssCursor, Long> {

	Optional<PostGroupRssCursor> findByPostGroup(PostGroup postGroup);
}
