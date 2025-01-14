package org.domainmodule.post.domain;

import java.time.LocalDateTime;

import org.domainmodule.post.domain.type.PostStatus;
import org.domainmodule.postgroup.domain.PostGroup;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_group_id")
	private PostGroup postGroup;

	private String title;

	private String summary;

	private String content;

	@Enumerated(EnumType.STRING)
	private PostStatus status;

	private LocalDateTime uploadAt;
}
