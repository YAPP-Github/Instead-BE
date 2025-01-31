package org.domainmodule.postgroup.repository;

import org.domainmodule.postgroup.entity.PostGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostGroupRepository extends JpaRepository<PostGroup, Long> {
}
