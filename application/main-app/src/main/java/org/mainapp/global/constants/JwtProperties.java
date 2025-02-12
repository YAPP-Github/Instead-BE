package org.mainapp.global.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private long accessTokenExpirationMS;
	private long refreshTokenExpirationMS;
	private String accessTokenKey;
	private String refreshTokenKey;
}
