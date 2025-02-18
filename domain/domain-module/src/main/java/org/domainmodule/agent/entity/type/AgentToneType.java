package org.domainmodule.agent.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AgentToneType {
	CASUAL("반말", "~해"),
	LESS_FORMAL("가벼운 존댓말", "~해요"),
	MORE_FORMAL("엄격한 존댓말", "~합니다"),
	CUSTOM(null, null);

	private final String value;
	private final String suffix;
}
