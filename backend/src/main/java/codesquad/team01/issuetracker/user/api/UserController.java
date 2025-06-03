package codesquad.team01.issuetracker.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.user.dto.UserDto;
import codesquad.team01.issuetracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {

	private final UserService userService;

	@GetMapping("/v1/users/filters")
	public ResponseEntity<ApiResponse<UserDto.UserFilterListResponse>> getUsers() {
		UserDto.UserFilterListResponse response = userService.findUsersForFilter();
		log.info("사용자 목록 개수= {}", response.totalCount());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
