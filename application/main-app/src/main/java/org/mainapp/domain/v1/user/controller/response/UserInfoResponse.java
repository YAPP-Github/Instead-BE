package org.mainapp.domain.v1.user.controller.response;

import org.domainmodule.user.entity.User;

public record UserInfoResponse(
	String name,
	String profileImage
) {
	public static UserInfoResponse of(User user) {
		return new UserInfoResponse(user.getName(), user.getProfileImage());
	}
}
