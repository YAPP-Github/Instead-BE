package com.yapp.web1team.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yapp.web1team.domain.user.entity.User;
import com.yapp.web1team.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// 유저가 존재하는지 확인
	public boolean existsByAccount(String account) {
		return userRepository.existsByAccount(account);
	}

	// 아이디로 유저 찾기
	public User findUserByAccount(String account) {
		return userRepository.findByAccount(account)
			.orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));
	}

	// 비밀번호 변경
	public void changePassword(String account, String newPassword) {
		User user = findUserByAccount(account);
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	// 유저 저장
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
}
