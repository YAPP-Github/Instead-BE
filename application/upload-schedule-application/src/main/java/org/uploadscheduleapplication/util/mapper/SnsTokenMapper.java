package org.uploadscheduleapplication.util.mapper;

import org.domainmodule.snstoken.entity.SnsToken;
import org.snsclient.twitter.dto.response.TwitterToken;

public class SnsTokenMapper {
	public static TwitterToken toTwitterToken(SnsToken token) {
		return TwitterToken.fromTokens(token.getAccessToken(), token.getRefreshToken());
	}
}
