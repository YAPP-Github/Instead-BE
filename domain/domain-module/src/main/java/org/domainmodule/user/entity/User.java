package org.domainmodule.user.entity;

import java.util.ArrayList;
import java.util.List;

import org.domainmodule.common.entity.BaseAuditEntity;
import org.domainmodule.sns.entity.SnsProvider;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SnsProvider> snsProviders = new ArrayList<>();

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
