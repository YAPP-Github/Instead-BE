package org.scheduleapp.snstoken;

import org.domainmodule.snstoken.entity.SnsToken;
import org.domainmodule.snstoken.repository.SnsTokenRepository;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.facade.TwitterApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.scheduleapp.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;
import twitter4j.TwitterException;

@Service
@RequiredArgsConstructor
public class SnsTokenService {
	private final TwitterApiService twitterApiService;
	private final SnsTokenRepository snsTokenRepository;

	@Transactional
	public UploadPostDto reissueToken(UploadPostDto uploadPostDto) throws TwitterException {

		//TODO 테이블 생성 후 값 찾아서 넣기
		String clientId ="df";

		// 토큰 재발급
		TwitterToken newSnsToken = twitterApiService.refreshTwitterToken(uploadPostDto.snsToken().getRefreshToken(), clientId);

		// Sns토큰 업데이트
		SnsToken snsToken = uploadPostDto.snsToken();
		snsToken.update(
			newSnsToken.accessToken(),
			newSnsToken.refreshToken(),
			newSnsToken.expiresIn()
		);
		snsTokenRepository.save(snsToken);

		// 기존 Post DTO에 새로운 토큰 설정
		return UploadPostDto.of(
			uploadPostDto.post(),
			snsToken
		);
	}
}
