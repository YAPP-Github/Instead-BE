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
	public SnsProvider findOrCreateSnsProvider(User user, String clientId, String clientSecret) {
		return snsProviderRepository.findByUserAndClientIdAndClientSecret(user, clientId, clientSecret)
			.orElseGet(() -> snsProviderRepository.save(SnsProvider.create(clientId, clientSecret, user)));
	}

	public SnsProvider findSnsProviderById(Long snsProviderId) {
		return snsProviderRepository.findById(snsProviderId)
			.orElseThrow(() -> new CustomException(SnsErrorCode.SNS_PROVIDER_NOT_FOUND));
	}
}
