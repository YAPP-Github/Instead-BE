package org.mainapplication.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@GetMapping("/yapp")
	public String checkHealth() {
		return "success";
	}
}
