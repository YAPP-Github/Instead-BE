package org.domainmodule.snstoken.entity;

import java.time.LocalDateTime;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.common.entity.BaseAuditEntity;

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
public class SnsToken extends BaseAuditEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sns_token_id")
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_id", nullable = false)
	private Agent agent;

	@Column(nullable = false, unique = true, length = 500)
	private String accessToken;

	@Column(nullable = false, unique = true, length = 500)
	private String refreshToken;

	@Column(nullable = false)
	private LocalDateTime accessTokenExpirationDate;

	@Column(nullable = false)
	private LocalDateTime refreshTokenExpirationDate;

	@Builder(access = lombok.AccessLevel.PRIVATE)
	private SnsToken(
		Agent agent,
		String accessToken,
		String refreshToken,
		LocalDateTime accessTokenExpirationDate,
		LocalDateTime refreshTokenExpirationDate
	) {
		this.agent = agent;
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.accessTokenExpirationDate = accessTokenExpirationDate;
		this.refreshTokenExpirationDate = refreshTokenExpirationDate;
	}

	public static SnsToken create(
		Agent agent,
		String accessToken,
		String refreshToken,
		Long accessTokenDurationInSeconds
	) {
		LocalDateTime now = LocalDateTime.now();

		return SnsToken.builder()
			.agent(agent)
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.accessTokenExpirationDate(now.plusSeconds(accessTokenDurationInSeconds))
			.refreshTokenExpirationDate(now.plusMonths(6)) // X refreshToken 6개월
			.build();
	}

	//TODO refreshToken 만료일 6개월 고정인거 이후에 인자로 수정
	public void update(
		String newAccessToken,
		String newRefreshToken,
		Long accessTokenDurationInSeconds
	) {
		LocalDateTime now = LocalDateTime.now();

		this.accessToken = newAccessToken;
		this.refreshToken = newRefreshToken;
		this.accessTokenExpirationDate = now.plusSeconds(accessTokenDurationInSeconds);
		this.refreshTokenExpirationDate = now.plusMonths(6); // X refreshToken 6개월
	}
}
