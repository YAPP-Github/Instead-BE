package org.domainmodule.agent.entity.type;

public enum AgentType {
	FREE, BASIC, PREMIUM, PREMIUM_PLUS ;
	public static AgentType fromSubscription(String subscriptionType) {
		if (subscriptionType == null) {
			return FREE;
		}
		return switch (subscriptionType.toUpperCase()) {
			case "BASIC" -> BASIC;
			case "PREMIUM" -> PREMIUM;
			case "PREMIUMPLUS" -> PREMIUM_PLUS;
			default -> FREE;
		};
	}
}
