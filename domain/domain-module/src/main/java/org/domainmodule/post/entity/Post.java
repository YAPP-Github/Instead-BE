package org.domainmodule.post.entity;

import java.time.LocalDateTime;

import org.domainmodule.common.entity.BaseAuditEntity;
import org.domainmodule.post.entity.type.PostStatus;
import org.domainmodule.postgroup.entity.PostGroup;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_group_id")
	private PostGroup postGroup;

	@Column(length = 500)
	private String title;

	@Column(length = 500)
	private String summary;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Enumerated(EnumType.STRING)
	private PostStatus status;

	@Column(name = "upload_time")
	private LocalDateTime uploadTime;

	public void setStatus(PostStatus status) {
		this.status = status;
	}
}
