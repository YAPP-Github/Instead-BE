package org.uploadscheduleapplication.snstoken;

import org.domainmodule.snstoken.entity.SnsToken;
import org.domainmodule.snstoken.repository.SnsTokenRepository;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnsTokenService {
	private final SnsTokenRepository snsTokenRepository;

	@Transactional
	public void updateSnsToken(SnsToken snsToken, TwitterToken twitterToken) {
		snsToken.update(
			twitterToken.accessToken(),
			twitterToken.refreshToken(),
			twitterToken.expiresIn()
		);
	}
}
