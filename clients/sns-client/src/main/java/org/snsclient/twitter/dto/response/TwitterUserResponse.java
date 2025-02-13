package org.snsclient.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwitterUserResponse(
	@JsonProperty("data") TwitterUserInfoDto data
) {}
