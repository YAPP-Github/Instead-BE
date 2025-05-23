package org.mainapp.domain.v1.post.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.domainmodule.agent.entity.Agent;
import org.domainmodule.agent.entity.AgentPersonalSetting;
import org.domainmodule.agent.repository.AgentPersonalSettingRepository;
import org.domainmodule.agent.repository.AgentRepository;
import org.domainmodule.post.entity.Post;
import org.domainmodule.post.entity.type.PostStatusType;
import org.domainmodule.post.entity.type.UploadTimeType;
import org.domainmodule.post.repository.PostRepository;
import org.domainmodule.postgroup.entity.PostGroup;
import org.domainmodule.postgroup.repository.PostGroupRepository;
import org.mainapp.domain.v1.agent.exception.AgentErrorCode;
import org.mainapp.domain.v1.post.controller.request.CreatePostsRequest;
import org.mainapp.domain.v1.post.controller.request.MultiplePostUpdateRequest;
import org.mainapp.domain.v1.post.controller.request.ReserveUploadTimeRequest;
import org.mainapp.domain.v1.post.controller.request.SinglePostUpdateRequest;
import org.mainapp.domain.v1.post.controller.request.UpdatePostContentRequest;
import org.mainapp.domain.v1.post.controller.request.UpdatePostGroupStepRequest;
import org.mainapp.domain.v1.post.controller.request.UpdatePostsMetadataRequest;
import org.mainapp.domain.v1.post.controller.request.UpdateReservedPostsRequest;
import org.mainapp.domain.v1.post.controller.request.type.UpdatePostsRequestItem;
import org.mainapp.domain.v1.post.controller.request.type.UpdateReservedPostsRequestItem;
import org.mainapp.domain.v1.post.controller.response.CreatePostsResponse;
import org.mainapp.domain.v1.post.controller.response.GetAgentReservedPostsResponse;
import org.mainapp.domain.v1.post.controller.response.GetPostGroupPostsResponse;
import org.mainapp.domain.v1.post.controller.response.GetPostGroupStepResponse;
import org.mainapp.domain.v1.post.controller.response.GetPostGroupTopicResponse;
import org.mainapp.domain.v1.post.controller.response.GetPostGroupsResponse;
import org.mainapp.domain.v1.post.controller.response.type.PostGroupResponse;
import org.mainapp.domain.v1.post.controller.response.type.PostResponse;
import org.mainapp.domain.v1.post.exception.PostErrorCode;
import org.mainapp.global.constants.PostGenerationCount;
import org.mainapp.global.error.CustomException;
import org.mainapp.global.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {

	private final PostCreateService postCreateService;
	private final PostUpdateService postUpdateService;
	private final PostPromptUpdateService postPromptUpdateService;
	private final PostTransactionService postTransactionService;
	private final AgentRepository agentRepository;
	private final AgentPersonalSettingRepository agentPersonalSettingRepository;
	private final PostGroupRepository postGroupRepository;
	private final PostRepository postRepository;

	/**
	 * 게시물 그룹 및 게시물 생성 메서드.
	 */
	public CreatePostsResponse createPosts(Long agentId, CreatePostsRequest request, Integer limit) {
		// 사용자 인증 정보 및 Agent 조회
		Long userId = SecurityUtil.getCurrentUserId();
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(userId,
			agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		return switch (request.getReference()) {
			case NONE -> postCreateService.createPostsWithoutRef(agentPersonalSetting, request, limit);
			case NEWS -> postCreateService.createPostsByNews(agentPersonalSetting, request, limit);
			case IMAGE -> postCreateService.createPostsByImage(agentPersonalSetting, request, limit);
		};
	}

	/**
	 * 게시물 추가 생성 메서드.
	 * postGroupId를 바탕으로 DB에서 PostGroup을 조회한 뒤, referenceType에 맞는 메서드 호출
	 */
	public CreatePostsResponse createAdditionalPosts(Long agentId, Long postGroupId, Integer limit) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// PostGroup의 게시물 생성 횟수 검증
		if (postGroup.getGenerationCount() >= PostGenerationCount.MAX_POST_GENERATION_COUNT) {
			throw new CustomException(PostErrorCode.EXHAUSTED_GENERATION_COUNT);
		}

		// Agent 조회
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(userId,
			agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// displayOrder 설정을 위해 Post 조회
		Integer order = postRepository.findLastGeneratedPost(postGroup, PostStatusType.GENERATED)
			.map(Post::getDisplayOrder)
			.orElse(0);

		// referenceType에 따라 분기
		return switch (postGroup.getReference()) {
			case NONE ->
				postCreateService.createAdditionalPostsWithoutRef(agentPersonalSetting, postGroup, limit, order);
			case NEWS -> postCreateService.createAdditionalPostsByNews(agentPersonalSetting, postGroup, limit, order);
			case IMAGE -> postCreateService.createAdditionalPostsByImage(agentPersonalSetting, postGroup, limit, order);
		};
	}

	/**
	 * 사용자 연동 SNS 계정별 게시물 그룹 목록을 반환하는 메서드
	 */
	public GetPostGroupsResponse getPostGroups(Long agentId) {
		// 사용자 인증 정보 및 PostGroup 리스트 최신순 조회
		Long userId = SecurityUtil.getCurrentUserId();
		List<PostGroup> postGroups = postGroupRepository.findAllByUserIdAndAgentIdOrderByLatest(userId, agentId);

		// 반환
		return GetPostGroupsResponse.from(postGroups);
	}

	/**
	 * 게시물 그룹을 단건 조회하는 메서드
	 */
	public PostGroupResponse getPostGroup(Long agentId, Long postGroupId) {
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));
		return PostGroupResponse.from(postGroup);
	}

	/**
	 * 게시물 그룹의 주제를 조회하는 메서드
	 */
	public GetPostGroupTopicResponse getPostGroupTopic(Long agentId, Long postGroupId) {
		Long userId = SecurityUtil.getCurrentUserId();
		String topic = postGroupRepository.findTopicByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));
		return new GetPostGroupTopicResponse(topic);
	}

	/**
	 * 게시물 그룹의 단계를 조회하는 메서드
	 */
	public GetPostGroupStepResponse getPostGroupStep(Long agentId, Long postGroupId) {
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));
		return GetPostGroupStepResponse.from(postGroup);
	}

	/**
	 * 게시물 그룹의 단계를 수정하는 메서드
	 */
	public void updatePostGroupStep(Long agentId, Long postGroupId, UpdatePostGroupStepRequest request) {
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		postGroup.updateStep(request.step());
		postTransactionService.savePostGroup(postGroup);
	}

	/**
	 * postGroupId를 바탕으로 게시물 그룹 존재 여부를 확인하고, 해당 그룹의 게시물 목록을 반환하는 메서드
	 * 게시물 그룹 조회 실패 시 POST_GROUP_NOT_FOUND
	 */
	public GetPostGroupPostsResponse getPostsByPostGroup(Long agentId, Long postGroupId) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		List<Post> posts = postRepository.findAllWithImagesByPostGroup(postGroup);

		// 상태별로 그룹화
		Map<PostStatusType, List<PostResponse>> groupedPosts = initializeEmptyStatusMap();
		posts.stream()
			.map(PostResponse::from)
			.forEach(post -> groupedPosts.get(post.getStatus()).add(post));

		// displayOrder 오름차순으로 정렬
		groupedPosts.forEach((status, list) ->
			list.sort(Comparator.comparingInt(PostResponse::getDisplayOrder))
		);

		return GetPostGroupPostsResponse.of(postGroup, groupedPosts);
	}

	/**
	 * PostStatusType을 포함하는 빈 리스트 생성
	 */
	private Map<PostStatusType, List<PostResponse>> initializeEmptyStatusMap() {
		return EnumSet.allOf(PostStatusType.class).stream()
			.collect(Collectors.toMap(
				Function.identity(),
				status -> new ArrayList<>()
			));
	}

	/**
	 * 단일 게시물을 prompt를 적용하여 업데이트하는 메서드
	 */
	public PostResponse updateSinglePostByPrompt(
		SinglePostUpdateRequest request, Long agentId, Long postGroupId, Long postId
	) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Agent 조회
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(userId,
			agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// Post 조회
		Post post = postTransactionService.getPostOrThrow(postGroup, postId);

		return postPromptUpdateService.updateSinglePostByPrompt(agentPersonalSetting, postGroup, post, request);
	}

	/**
	 * 일괄로 게시물들을 prompt 적용 후 업데이트 하는 메서드
	 */
	public List<PostResponse> updateMultiplePostsByPrompt(
		MultiplePostUpdateRequest request, Long agentId, Long postGroupId
	) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Agent 조회
		AgentPersonalSetting agentPersonalSetting = agentPersonalSettingRepository.findByUserIdAndAgentId(userId,
			agentId).orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// Post 리스트 조회
		List<Post> posts = request.postsId().stream()
			.map(postId -> postTransactionService.getPostOrThrow(postGroup, postId))
			.toList();

		return postPromptUpdateService.updateMultiplePostsByPrompt(agentPersonalSetting, postGroup, posts, request);
	}

	/**
	 * 게시물 내용 수정 메서드.
	 */
	public void updatePostContent(Long agentId, Long postGroupId, Long postId, UpdatePostContentRequest request) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 조회
		Post post = postRepository.findWithImagesByPostGroupAndId(postGroup, postId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

		postUpdateService.updatePostContent(post, request);
		post.updateStatus(PostStatusType.EDITING);
	}

	/**
	 * 게시물 기타 정보 수정 메서드.
	 */
	public void updatePostsMetadata(Long agentId, Long postGroupId, UpdatePostsMetadataRequest request) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 리스트 조회
		List<Long> postIds = request.getPosts().stream()
			.map(UpdatePostsRequestItem::getPostId)
			.toList();
		List<Post> posts = postRepository.findAllByPostGroupAndId(postGroup, postIds);

		postUpdateService.updatePostsMetadata(posts, request);
	}

	/**
	 * 계정별 예약 게시물 예약일시 수정 메서드.
	 */
	public void updateReservedPostsUploadTime(Long agentId, UpdateReservedPostsRequest request) {
		// 사용자 인증 정보 및 Agent 조회
		Long userId = SecurityUtil.getCurrentUserId();
		Agent agent = agentRepository.findByUserIdAndId(userId, agentId)
			.orElseThrow(() -> new CustomException(AgentErrorCode.AGENT_NOT_FOUND));

		// postId 리스트 추출
		List<Long> postIds = request.posts().stream()
			.map(UpdateReservedPostsRequestItem::postId)
			.toList();

		// Post 리스트 조회
		List<Post> posts = postRepository.findAllByAgentAndId(agent, postIds);

		postUpdateService.updateReservedPostsUploadTime(posts, request);
	}

	/**
	 *  업로드 시간 빠른 예약하는 메서드
	 */
	public GetAgentReservedPostsResponse reserveReadyToUploadPosts(Long agentId, Long postGroupId,
		ReserveUploadTimeRequest request) {
		Long userId = SecurityUtil.getCurrentUserId();

		// PostGroup의 예약 시간 정할 글 (displayOrder ASC 정렬)
		List<Post> readyToUploadPosts = postRepository.findPostsByUserAndAgentAndStatus(
			userId, agentId, postGroupId, PostStatusType.READY_TO_UPLOAD);

		// 해당 Agent의 이미 예약 시간 확정 + 업로드 대기 상태인 글
		List<PostStatusType> postStatusTypeList = List.of(
			PostStatusType.UPLOAD_RESERVED,
			PostStatusType.UPLOAD_CONFIRMED
		);
		List<Post> confirmedUploadPosts = postRepository.findAllReservedPostsByUserAndAgentAndStatus(
			userId, agentId, postStatusTypeList);

		// 하루에 업로드할 개수, 업로드 시작 날짜, 업로드할 전체
		int dailyUploadCount = request.dailyUploadCount();
		LocalDate startDate = request.uploadStartDate();
		int totalPosts = readyToUploadPosts.size();

		// 업로드할 시간대 범위 가져오기
		UploadTimeType uploadTimeType = request.uploadTimeType();
		LocalTime timeSlotStart = uploadTimeType.getStartTime();
		LocalTime timeSlotEnd = uploadTimeType.getEndTime();

		// 기존 예약된 게시물의 업로드 시간 저장
		Map<LocalDate, NavigableSet<LocalDateTime>> reservedTimesByDate = confirmedUploadPosts.stream()
			.collect(Collectors.groupingBy(
				post -> post.getUploadTime().toLocalDate(),
				Collectors.mapping(Post::getUploadTime, Collectors.toCollection(TreeSet::new))
			));

		// 하루에 업로드할 개수를 고려하여 랜덤한 업로드 시간 생성
		List<LocalDateTime> randomAvailableTimes = generateRandomUploadTimes(
			startDate, timeSlotStart, timeSlotEnd, totalPosts, dailyUploadCount, reservedTimesByDate
		);

		// 랜덤한 시간을 오름차순 정렬
		Collections.sort(randomAvailableTimes);

		// readyToUploadPosts에 예약 시간 배정
		for (int i = 0; i < totalPosts; i++) {
			Post post = readyToUploadPosts.get(i);
			post.updateUploadTime(randomAvailableTimes.get(i));
			post.updateStatus(PostStatusType.UPLOAD_RESERVED);
		}

		// 변경된 post 저장
		postRepository.saveAll(readyToUploadPosts);

		return GetAgentReservedPostsResponse.from(readyToUploadPosts);
	}

	/**
	 * 랜덤한 업로드 시간을 생성하는 메서드 (하루 업로드 제한 적용)
	 */
	private List<LocalDateTime> generateRandomUploadTimes(LocalDate startDate, LocalTime start, LocalTime end,
		int totalPosts, int dailyUploadCount, Map<LocalDate, NavigableSet<LocalDateTime>> reservedTimesByDate) {

		List<LocalDateTime> availableTimes = new ArrayList<>();
		Random random = new Random();
		LocalDate currentDate = startDate;
		int assignedCount = 0;

		while (availableTimes.size() < totalPosts) {
			// 하루에 올릴 개수 초과 시, 다음 날로 이동
			if (assignedCount >= dailyUploadCount) {
				assignedCount = 0;
				currentDate = currentDate.plusDays(1);
			}

			// 랜덤한 분(minute) 생성 (start ~ end 사이)
			int randomMinute = random.nextInt((int)Duration.between(start, end).toMinutes());
			LocalDateTime randomTime = LocalDateTime.of(currentDate, start.plusMinutes(randomMinute));

			// 기존 예약된 시간과 겹치지 않으면 추가
			if (!reservedTimesByDate.getOrDefault(currentDate, new TreeSet<>()).contains(randomTime)) {
				availableTimes.add(randomTime);
				assignedCount++;
			}
		}
		return availableTimes;
	}

	/**
	 * 업로드가 확정되지 않은 상태의 게시물을 단건 삭제하는 메서드
	 */
	public void deletePost(Long agentId, Long postGroupId, Long postId) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 조회
		Post post = postRepository.findByPostGroupAndId(postGroup, postId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));

		// 검증: 삭제하려는 Post의 상태 확인
		validateDeletingPostStatus(post);

		// Post 엔티티 삭제
		postTransactionService.deletePost(post);
	}

	/**
	 * 업로드가 확정되지 않은 상태의 게시물들을 일괄 삭제하는 메서드
	 */
	public void deletePosts(Long agentId, Long postGroupId, List<Long> postIds) {
		// 사용자 인증 정보 및 PostGroup 조회
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		// Post 엔티티 리스트 조회
		// TODO: 지금은 단순히 조회한 Post의 개수를 가지고 검증하고 있는데, 상세 postId를 반환하기 위해 로직을 수정할 필요가 있을듯. 지금은 에러메시지에 변수를 담을 수 없는 상황이라 단순한 로직으로 구현함
		List<Post> posts = postRepository.findAllByPostGroupAndId(postGroup, postIds);
		if (posts.size() < postIds.size()) {
			throw new CustomException(PostErrorCode.POST_NOT_FOUND);
		}

		// 검증: PostGroup에 해당하는 Post가 맞는지 검증 & 삭제하려는 Post의 상태 확인
		posts.forEach(post -> {
			if (!post.getPostGroup().getId().equals(postGroupId)) {
				throw new CustomException(PostErrorCode.INVALID_POST);
			}
			validateDeletingPostStatus(post);
		});

		// Post 엔티티 리스트 삭제
		postTransactionService.deletePosts(posts);
	}

	/**
	 * 삭제하려는 Post가 업로드 확정되지 않은 상태인지 확인하는 메서드
	 */
	private void validateDeletingPostStatus(Post post) {
		// 삭제 가능한 상태: 생성됨, 수정중, 수정완료
		List<PostStatusType> validStatuses = List.of(
			PostStatusType.GENERATED,
			PostStatusType.EDITING,
			PostStatusType.READY_TO_UPLOAD,
			PostStatusType.UPLOAD_RESERVED);

		if (!validStatuses.contains(post.getStatus())) {
			throw new CustomException(PostErrorCode.INVALID_DELETING_POST_STATUS);
		}
	}

	/**
	 * 예약중인 게시물 조회
	 */
	public GetAgentReservedPostsResponse getAgentReservedPosts(Long agentId) {
		Long userId = SecurityUtil.getCurrentUserId();
		List<Post> posts = postRepository.findAllReservedPostsByUserAndAgent(userId, agentId,
			PostStatusType.UPLOAD_CONFIRMED);

		return GetAgentReservedPostsResponse.from(posts);
	}

	/**
	 * 단일 Post 상세내용 조회
	 */
	public PostResponse getPostDetails(Long agentId, Long postGroupId, Long postId) {
		Long userId = SecurityUtil.getCurrentUserId();

		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));

		Post post = postRepository.findByPostGroupAndId(postGroup, postId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_NOT_FOUND));
		return PostResponse.from(post);
	}

	/**
	 * PostGroup 삭제 - Group에 속한 모든 Post 제거
	 */
	@Transactional
	public void deletePostGroup(Long agentId, Long postGroupId) {
		Long userId = SecurityUtil.getCurrentUserId();
		PostGroup postGroup = postGroupRepository.findByUserIdAndAgentIdAndId(userId, agentId, postGroupId)
			.orElseThrow(() -> new CustomException(PostErrorCode.POST_GROUP_NOT_FOUND));
		List<Post> posts = postRepository.findAllByPostGroup(postGroup);

		postRepository.deleteAll(posts);
		postGroupRepository.delete(postGroup);
	}
}
