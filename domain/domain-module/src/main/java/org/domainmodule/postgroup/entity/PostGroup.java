package org.domainmodule.postgroup.entity;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.postgroup.entity.type.PostGroupPurpose;
import org.domainmodule.postgroup.entity.type.PostGroupReference;
import org.domainmodule.postgroup.entity.type.PostLength;
import org.domainmodule.rssfeed.entity.RssFeed;

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
public class PostGroup extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_group_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id")
	private Agent agent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id")
	private RssFeed feed;

	@Column(nullable = false, length = 255)
	private String topic;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostGroupPurpose purpose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostGroupReference reference;

	@Enumerated(EnumType.STRING)
	private PostLength length;

	@Column(columnDefinition = "TEXT")
	private String content;
}
