package org.domainmodule.agent.entity;

import org.domainmodule.agent.entity.type.AgentToneType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class AgentPersonalSetting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "personal_setting_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id")
	private Agent agent;

	@Column(length = 20)
	private String domain;

	@Column(length = 500)
	private String introduction;

	@Enumerated(EnumType.STRING)
	private AgentToneType tone;

	@Column(length = 50)
	private String customTone;

	@Builder(access = AccessLevel.PRIVATE)
	private AgentPersonalSetting(
		Agent agent, String domain, String introduction, AgentToneType tone, String customTone) {
		this.agent = agent;
		this.domain = domain;
		this.introduction = introduction;
		this.tone = tone;
		this.customTone = customTone;
	}

	public static AgentPersonalSetting create(
		Agent agent, String domain, String introduction, AgentToneType tone, String customTone) {
		return AgentPersonalSetting.builder()
			.agent(agent)
			.domain(domain)
			.introduction(introduction)
			.tone(tone)
			.customTone(customTone)
			.build();
	}

	public void updateDomain(String domain) {
		this.domain = domain;
	}

	public void updateIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public void updateTone(AgentToneType tone) {
		this.tone = tone;
	}

	public void updateCustomTone(String customTone) {
		this.customTone = customTone;
	}
}
