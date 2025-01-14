package org.domainmodule.agent.domain;

import org.domainmodule.agent.domain.type.AgentPlatform;
import org.domainmodule.agent.domain.type.AgentType;
import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.user.domain.User;

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
public class Agent extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "agent_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	private AgentPlatform platform;

	private String accessToken;

	private String refreshToken;

	private String accountId;

	private String bio;

	private Boolean autoMode;

	@Enumerated(EnumType.STRING)
	private AgentType agentType;
}
