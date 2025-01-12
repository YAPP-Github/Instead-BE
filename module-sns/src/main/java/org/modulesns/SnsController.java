package org.modulesns;

import org.modulecommon.service.CommonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SnsController {

	private final CommonService commonService;

	@GetMapping("/sns-message")
	public String getSnsMessage() {
		return commonService.getCommonMessage();
	}

}
