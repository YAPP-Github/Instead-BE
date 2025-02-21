package org.domainmodule.rssfeed.repository;

import java.util.List;
import java.util.Optional;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {

	Optional<RssFeed> findByCategory(FeedCategoryType category);

	@Query("""
			select rf from RssFeed rf
			where rf.isActivated = true
		""")
	List<RssFeed> findAllActivated();
}
