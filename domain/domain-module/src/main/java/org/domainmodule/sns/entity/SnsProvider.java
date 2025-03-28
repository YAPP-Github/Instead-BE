package org.domainmodule.sns.entity;

import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.user.entity.User;

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
public class SnsProvider extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "sns_provider")
	private Long id;

	@Column(nullable = false, unique = true)
	private String clientId;

	@Column(nullable = false, unique = true)
	private String clientSecret;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Builder(access = AccessLevel.PRIVATE)
	private SnsProvider(
		String clientId,
		String clientSecret,
		User user
	) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.user = user;
	}

	public static SnsProvider create(
		String clientId,
		String clientSecret,
		User user
	) {
		return SnsProvider.builder()
			.clientId(clientId)
			.clientSecret(clientSecret)
			.user(user)
			.build();
	}
}
