package org.snsclient.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TwitterUserInfoDto(
	@Schema(description = "계정 설명", example = "나는 친근이에요")
	@JsonProperty("description")
	String description,
	@Schema(description = "사용자의 X 구독 유형", example = "Basic, Premium, PremiumPlus, None(무료)")
	@JsonProperty("subscription_type")
	String subscriptionType,
	@Schema(description = "계정 이름", example = "친근이")
	@JsonProperty("username")
	String username,
	@Schema(description = "사용자 이름", example = "박필두")
	@JsonProperty("name")
	String name,
	@Schema(description = "프로필 이미지 url", example = "https://iamge.url")
	@JsonProperty("profile_image_url")
	String profileImageUrl,
	@Schema(description = "X 사용자 고유 ID", example = "14232423")
	@JsonProperty("id")
	String id
) {}
