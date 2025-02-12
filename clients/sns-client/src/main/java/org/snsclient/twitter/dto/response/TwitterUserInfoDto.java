package org.snsclient.twitter.dto.response;

import twitter4j.User2;

public record TwitterUserInfoDto(
	String id,
	String accountId,
	String name,
	String description,
	String profileImageUrl
) {

	public static TwitterUserInfoDto fromTwitterUser(User2 user) {
		return new TwitterUserInfoDto(
			String.valueOf(user.getId()),
			user.getScreenName(),
			user.getName(),
			user.getDescription(),
			user.getProfileImageUrl()
		);
	}
}
