package org.domainmodule.user.entity;

import org.domainmodule.common.entity.BaseAuditEntity;
import org.domainmodule.snstoken.entity.SnsToken;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseAuditEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@OneToOne(mappedBy = "agent", fetch = FetchType.LAZY)
	private SnsToken snsToken;

	@Builder
	private User (
		String email,
		String name

	) {
		this.email = email;
		this.name = name;
	}

	public static User createUser(
		String email,
		String name
	) {
		return User.builder()
			.email(email)
			.name(name)
			.build();
	}
}
