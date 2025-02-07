package org.domainmodule.postgroup.entity;

import java.util.ArrayList;
import java.util.List;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.postgroup.entity.type.PostGroupLengthType;
import org.domainmodule.postgroup.entity.type.PostGroupPurposeType;
import org.domainmodule.postgroup.entity.type.PostGroupReferenceType;
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
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
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

	@Column(nullable = false, length = 255)
	private String topic;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostGroupPurposeType purpose;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PostGroupReferenceType reference;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "feed_id")
	private RssFeed feed;

	@OneToMany(mappedBy = "postGroup")
	private List<PostGroupImage> postGroupImages = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private PostGroupLengthType length;

	@Column(columnDefinition = "TEXT")
	private String content;

	private Integer generationCount;

	@Builder
	private PostGroup(Agent agent, RssFeed feed, String topic, PostGroupPurposeType purpose,
		PostGroupReferenceType reference, PostGroupLengthType length, String content, Integer generationCount) {
		this.agent = agent;
		this.topic = topic;
		this.purpose = purpose;
		this.reference = reference;
		this.feed = feed;
		this.length = length;
		this.content = content;
		this.generationCount = generationCount;
	}

	public static PostGroup createPostGroup(
		Agent agent,
		RssFeed feed,
		String topic,
		PostGroupPurposeType purpose,
		PostGroupReferenceType reference,
		PostGroupLengthType length,
		String content,
		Integer generationCount
	) {
		return PostGroup.builder()
			.agent(agent)
			.feed(feed)
			.topic(topic)
			.purpose(purpose)
			.reference(reference)
			.length(length)
			.content(content)
			.generationCount(generationCount)
			.build();
	}

	public void increaseGenerationCount() {
		this.generationCount++;
	}

	@Override
	public String toString() {
		return feed + "\n"
			+ topic + "\n"
			+ purpose + "\n"
			+ reference + "\n"
			+ length + "\n"
			+ content + "\n";
	}
}
