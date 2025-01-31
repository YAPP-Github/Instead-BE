package org.domainmodule.rssfeed.repository;

import java.util.Optional;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.domainmodule.rssfeed.entity.type.FeedCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {

	Optional<RssFeed> findByCategory(FeedCategoryType category);
}
