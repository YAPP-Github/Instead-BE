package org.snsclient.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwitterUserInfoDto(
	@JsonProperty("description") String description,
	@JsonProperty("subscription_type") String subscriptionType,
	@JsonProperty("username") String username,
	@JsonProperty("name") String name,
	@JsonProperty("profile_image_url") String profileImageUrl,
	@JsonProperty("id") String id
) {}