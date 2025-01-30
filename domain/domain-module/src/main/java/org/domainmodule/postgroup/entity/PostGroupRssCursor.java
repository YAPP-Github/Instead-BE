package org.domainmodule.postgroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostGroupRssCursor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "rss_cursor_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_group_id")
	private PostGroup postGroup;

	private String newsId;

	@Builder
	private PostGroupRssCursor(PostGroup postGroup, String newsId) {
		this.postGroup = postGroup;
		this.newsId = newsId;
	}

	public PostGroupRssCursor createPostGroupRssCursor(PostGroup postGroup, String newsId) {
		return PostGroupRssCursor.builder()
			.postGroup(postGroup)
			.newsId(newsId)
			.build();
	}
}
