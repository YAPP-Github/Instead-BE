package org.domainmodule.agent.entity.type;

public enum AgentType {
	BASIC, PREMIUM;
	public static AgentType fromSubscription(String subscriptionType) {
		if (subscriptionType == null) {
			return BASIC;
		}
		return switch (subscriptionType.toUpperCase()) {
			case "PREMIUM" -> PREMIUM;
			default -> BASIC;
		};
	}
}
