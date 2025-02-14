package org.scheduleapp.util.mapper;

import org.snsclient.twitter.dto.response.TwitterToken;
import org.scheduleapp.util.dto.UploadPostDto;

public class SnsTokenMapper {
	public static TwitterToken toTwitterToken(UploadPostDto token) {
		return TwitterToken.of(token.snsToken().getAccessToken(), token.snsToken().getRefreshToken(), 0);
	}

}
