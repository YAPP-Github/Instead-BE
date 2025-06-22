package org.mainapp.domain.v1.newscategory.controller;

import java.util.List;

import org.mainapp.domain.v1.newscategory.controller.response.NewsCategoryResponse;
import org.mainapp.domain.v1.newscategory.service.NewsCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/news-categories")
@RequiredArgsConstructor
@Tag(name = "NewsCategory API", description = "뉴스 카테고리에 대한 요청을 처리하는 API입니다.")
public class NewsCategoryController {

	private final NewsCategoryService newsCategoryService;

	@Operation(summary = "뉴스 카테고리 목록 조회 API", description = "서비스의 전체 뉴스 카테고리 목록을 조회합니다.")
	@GetMapping
	public ResponseEntity<List<NewsCategoryResponse>> getNewsCategories() {
		return ResponseEntity.ok().body(newsCategoryService.getNewsCategories());
	}
}
