package org.uploadscheduleapplication.snstoken;

import org.domainmodule.snstoken.entity.SnsToken;
import org.domainmodule.snstoken.repository.SnsTokenRepository;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.service.TwitterApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uploadscheduleapplication.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnsTokenService {
	private final TwitterApiService twitterApiService;

	public void updateSnsToken(SnsToken snsToken, TwitterToken twitterToken) {
		snsToken.update(
			twitterToken.accessToken(),
			twitterToken.refreshToken(),
			twitterToken.expiresIn()
		);
	}

	@Transactional
	public UploadPostDto reissueToken(UploadPostDto uploadPostDto) {

		// 토큰 재발급
		TwitterToken newSnsToken = twitterApiService.refreshTwitterToken(uploadPostDto.snsToken().getRefreshToken());

		// Sns토큰 업데이트
		SnsToken snsToken = uploadPostDto.snsToken();
		updateSnsToken(snsToken, newSnsToken);

		// 기존 Post DTO에 새로운 토큰 설정
		return UploadPostDto.of(
			uploadPostDto.post(),
			snsToken
		);
	}
}
