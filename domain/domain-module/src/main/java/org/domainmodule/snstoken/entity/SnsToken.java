package org.domainmodule.snstoken.entity;

import java.time.LocalDateTime;

import org.domainmodule.common.entity.BaseAuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
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

	@Column(nullable = false, unique = true, length = 500)
	private String accessToken;

	@Column(nullable = false, unique = true, length = 500)
	private String refreshToken;

	@Column(nullable = false)
	private LocalDateTime accessTokenExpirationDate;

	@Column(nullable = false)
	private LocalDateTime refreshTokenExpirationDate;
}
