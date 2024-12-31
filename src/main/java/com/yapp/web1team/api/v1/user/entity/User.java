package com.yapp.web1team.api.v1.user.entity;

import org.hibernate.annotations.Comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Comment("아이디")
	@Column(columnDefinition = "varchar(255)", nullable = false, unique = true)
	private String account;

	@Comment("비밀번호")
	@Column(columnDefinition = "varchar(255)", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Builder
	private User (
		String account,
		String password,
		UserRole role
	) {
		this.account = account;
		this.password = password;
		this.role = role;
	}

	public static User create(
		String account,
		String encodedPassword) {
		return User.builder()
			.account(account)
			.password(encodedPassword)
			.role(UserRole.USER)
			.build();
	}
}