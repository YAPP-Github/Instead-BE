package org.scheduleapp;

import org.scheduleapp.schedule.UploadPostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class controller {
	private final UploadPostService uploadPostService;

	@GetMapping("/")
	public String test() {
		uploadPostService.uploadPosts();
		return  "sdf";
	}
}
