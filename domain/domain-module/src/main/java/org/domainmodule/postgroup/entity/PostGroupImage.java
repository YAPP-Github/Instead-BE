package org.domainmodule.postgroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostGroupImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_group_image_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_group_id")
	private PostGroup postGroup;

	@Column(length = 500)
	private String url;

	@Builder(access = AccessLevel.PRIVATE)
	private PostGroupImage(PostGroup postGroup, String url) {
		this.postGroup = postGroup;
		this.url = url;
	}

	public static PostGroupImage createPostGroupImage(PostGroup postGroup, String url) {
		return PostGroupImage.builder()
			.postGroup(postGroup)
			.url(url)
			.build();
	}
}
