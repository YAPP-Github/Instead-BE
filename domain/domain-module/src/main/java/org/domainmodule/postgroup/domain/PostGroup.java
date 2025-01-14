package org.domainmodule.postgroup.domain;

import org.domainmodule.agent.domain.Agent;
import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.postgroup.domain.type.PostGroupPurpose;
import org.domainmodule.postgroup.domain.type.PostGroupReference;
import org.domainmodule.rssfeed.domain.RssFeed;

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

	private String topic;

	@Enumerated(EnumType.STRING)
	private PostGroupPurpose purpose;

	@Enumerated(EnumType.STRING)
	private PostGroupReference reference;

	private Integer length;

	private String content;
}
