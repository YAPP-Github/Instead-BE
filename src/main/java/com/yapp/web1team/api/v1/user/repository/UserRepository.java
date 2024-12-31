package com.yapp.web1team.api.v1.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yapp.web1team.api.v1.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByAccount(String account);

	boolean existsByAccount(String accout);
}
