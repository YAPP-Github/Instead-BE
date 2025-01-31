package org.uploadscheduleapplication.util.mapper;

import org.snsclient.twitter.dto.response.TwitterToken;
import org.uploadscheduleapplication.util.dto.UploadPostDto;

public class SnsTokenMapper {
	public static TwitterToken toTwitterToken(UploadPostDto token) {
		return TwitterToken.fromFields(token.snsToken().getAccessToken(), token.snsToken().getRefreshToken(), 0);
	}

}
