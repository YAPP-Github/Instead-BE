package com.yapp.web1team.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yapp.web1team.domain.user.entity.User;
import com.yapp.web1team.domain.user.repository.UserRepository;
import com.yapp.web1team.security.entity.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
		User user = userRepository.findByAccount(account)
			.orElseThrow(() -> new UsernameNotFoundException("유저가 없습니다"));

		return new CustomUserDetails(user);
	}
}
