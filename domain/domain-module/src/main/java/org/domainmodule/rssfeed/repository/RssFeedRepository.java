package org.domainmodule.rssfeed.repository;

import org.domainmodule.rssfeed.entity.RssFeed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RssFeedRepository extends JpaRepository<RssFeed, Long> {
}
