package org.domainmodule.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Getter;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public class BaseAuditEntity extends BaseTimeEntity {

	@Column(updatable = true, nullable = false)
	private LocalDateTime updatedAt;

	@Transient
	private boolean preventUpdatedAt = false;

	@PrePersist
	public void prePersist() {
		this.updatedAt = LocalDateTime.now(); // 초기 저장 시 updatedAt 설정
	}

	@PreUpdate
	public void setPreventUpdatedAt() {
		// 업데이트가 가능하면 현재시간으로 설정
		if (!isPreventUpdatedAt()) {
			this.updatedAt = LocalDateTime.now();
		}
	}

	public void markPreventUpdatedAt() {
		this.preventUpdatedAt = true;
	}

	private boolean isPreventUpdatedAt() {
		return preventUpdatedAt;
	}
}
