package org.scheduleapp.snstoken;

import org.domainmodule.snstoken.entity.SnsToken;
import org.domainmodule.snstoken.repository.SnsTokenRepository;
import org.snsclient.twitter.dto.response.TwitterToken;
import org.snsclient.twitter.service.Twitter4jService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.scheduleapp.util.dto.UploadPostDto;

import lombok.RequiredArgsConstructor;
import twitter4j.TwitterException;

@Service
@RequiredArgsConstructor
public class SnsTokenService {
	private final Twitter4jService twitter4JService;
	private final SnsTokenRepository snsTokenRepository;

	@Transactional
	public UploadPostDto reissueToken(UploadPostDto uploadPostDto) {

		// 토큰 재발급
		TwitterToken newSnsToken = refreshSnsToken(uploadPostDto.snsToken().getRefreshToken());

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

	private TwitterToken refreshSnsToken(String refreshToken) {
		try {
			return twitter4JService.refreshTwitterToken(refreshToken);
		} catch (TwitterException e) {
			throw new RuntimeException("Twitter 토큰 재발급에 실패하였습니다.", e);
		}
	}
}
