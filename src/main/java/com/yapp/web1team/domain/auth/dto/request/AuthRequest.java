package com.yapp.web1team.domain.auth.dto.request;

public record AuthRequest(
	String account,
	String password
) {
}
