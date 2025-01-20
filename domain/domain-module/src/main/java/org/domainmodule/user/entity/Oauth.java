package org.domainmodule.user.entity;

import org.domainmodule.common.entity.BaseTimeEntity;
import org.domainmodule.user.entity.type.ProviderType;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Oauth extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "oauth_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	private ProviderType provider;

	@Column(nullable = false, unique = true, length = 100)
	private String providerId;

	@Builder
	private Oauth (
		User user,
		ProviderType provider,
		String providerId

	) {
		this.user = user;
		this.provider = provider;
		this.providerId = providerId;
	}

	public static Oauth createOauth(
		User user,
		ProviderType provider,
		String providerId) {
		return Oauth.builder()
			.user(user)
			.provider(provider)
			.providerId(providerId)
			.build();
	}
}
