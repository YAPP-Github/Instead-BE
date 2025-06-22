package org.domainmodule.user.entity;

import org.domainmodule.common.entity.BaseAuditEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Column(length = 500)
	private String profileImage;

	@Builder(access = AccessLevel.PRIVATE)
	private User(
		String email,
		String name,
		String profileImage
	) {
		this.email = email;
		this.name = name;
		this.profileImage = profileImage;
	}

	public static User createUser(
		String email,
		String name,
		String profileImage
	) {
		return User.builder()
			.email(email)
			.name(name)
			.profileImage(profileImage)
			.build();
	}
}
