package org.mainapp.global.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationPropertiesScan
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private long accessTokenExpirationMs;
	private long refreshTokenExpirationMs;
	private String accessTokenKey;
	private String refreshTokenKey;
}
