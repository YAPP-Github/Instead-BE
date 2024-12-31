package com.yapp.web1team.api.v1.auth.dto.request;

public record AuthRequest(
	String account,
	String password
) {
}
