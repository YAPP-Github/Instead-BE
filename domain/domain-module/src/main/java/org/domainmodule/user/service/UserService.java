package org.domainmodule.user.service;

import org.commonmodule.dto.OAuth2Response;
import org.domainmodule.user.entity.User;

public interface UserService {
	User loginOrCreateUser(OAuth2Response oAuth2Response);
	User registerUser(OAuth2Response oAuth2Response);
}
