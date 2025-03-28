package org.domainmodule.agent.entity;

import org.domainmodule.agent.entity.type.AgentPlanType;
import org.domainmodule.agent.entity.type.AgentPlatformType;
import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.sns.entity.SnsToken;
import org.domainmodule.user.entity.User;

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
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Agent extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "agent_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AgentPlatformType platform;

	@Column(nullable = false, unique = true, length = 100)
	private String accountId;

	@Column(length = 200)
	private String accountName;

	@Column(length = 255)
	private String bio;

	@Column(length = 500)
	private String profileImage;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AgentPlanType agentPlan;

	@Column(nullable = false)
	private Boolean autoMode;

	@Column(nullable = false)
	private Boolean isActivated;

	// TODO: OneToOne 관계에서 mappedBy 필드는 지연 로딩 설정해놔도 즉시 로딩으로 동작한다고 함, 여기서 추가 쿼리 계속 발생.
	@OneToOne(mappedBy = "agent", fetch = FetchType.LAZY)
	private SnsToken snsToken;

	@Builder(access = lombok.AccessLevel.PRIVATE)
	private Agent(
		User user,
		AgentPlatformType agentPlatform,
		String accountId,
		String bio,
		String profileImage,
		String accountName,
		AgentPlanType agentPlan
	) {
		this.user = user;
		this.platform = agentPlatform;
		this.accountId = accountId;
		this.bio = bio;
		this.profileImage = profileImage;
		this.accountName = accountName;
		this.autoMode = Boolean.FALSE;
		this.agentPlan = agentPlan;
		this.isActivated = Boolean.TRUE;
	}

	public static Agent create(
		User user,
		AgentPlatformType agentPlatform,
		String accountId,
		String bio,
		String profileImage,
		String subscriptionType,
		String accountName
	) {
		return Agent.builder()
			.user(user)
			.agentPlatform(agentPlatform)
			.accountId(accountId)
			.bio(bio)
			.profileImage(profileImage)
			.agentPlan(AgentPlanType.fromSubscription(subscriptionType))
			.accountName(accountName)
			.build();
	}

	public void updateInfo(
		String bio,
		String profileImage,
		String agentType,
		String accountName

	) {
		this.bio = bio;
		this.profileImage = profileImage;
		this.agentPlan = AgentPlanType.fromSubscription(agentType);
		this.accountName = accountName;
	}
}
