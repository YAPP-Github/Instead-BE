package org.mainapp.domain.agent.controller.response.type;

import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.entity.type.AgentToneType;

public record AgentPersonalSettingResponse(
	String domain,
	String introduction,
	AgentToneType tone,
	String customTone
) {

	public static AgentPersonalSettingResponse from(AgentPersonalSetting agentPersonalSetting) {
		return new AgentPersonalSettingResponse(
			agentPersonalSetting.getDomain(),
			agentPersonalSetting.getIntroduction(),
			agentPersonalSetting.getTone(),
			agentPersonalSetting.getCustomTone()
		);
	}
}
