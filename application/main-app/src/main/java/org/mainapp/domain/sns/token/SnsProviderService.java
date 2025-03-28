package org.mainapp.domain.sns.token;

import org.domainmodule.sns.entity.SnsProvider;
import org.domainmodule.sns.repository.SnsProviderRespository;
import org.domainmodule.user.entity.User;
import org.mainapp.domain.sns.exception.SnsErrorCode;
import org.mainapp.global.error.CustomException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnsProviderService {
	private final SnsProviderRespository snsProviderRepository;

	@Transactional
	public SnsProvider createSnsProvider(User user, String clientId, String clientSecret) {
		// 이미 등록된 clientid, clientsecret이면 예외 (2개의 구글ID에 똑같은 SNS등록 방지)
		if (snsProviderRepository.existsByClientIdAndClientSecret(clientId, clientSecret)) {
			throw new CustomException(SnsErrorCode.SNS_PROVIDER_ALREADY_EXISTS);
		}

		return snsProviderRepository.save(SnsProvider.create(clientId, clientSecret, user));
	}

	public SnsProvider findSnsProviderById(Long snsProviderId) {
		return snsProviderRepository.findById(snsProviderId)
			.orElseThrow(() -> new CustomException(SnsErrorCode.SNS_PROVIDER_NOT_FOUND));
	}
}
