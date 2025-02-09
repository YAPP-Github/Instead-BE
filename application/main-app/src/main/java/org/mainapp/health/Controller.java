package org.mainapp.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/yapp")
public class Controller {

	@GetMapping("/")
	public String checkHealth() {
		return "success";
	}
}
