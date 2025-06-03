package codesquad.team01.issuetracker.issue.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.common.annotation.CursorParam;
import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.issue.service.IssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class IssueController {

	private final IssueService issueService;

	@GetMapping("/v1/issues")
	public ResponseEntity<ApiResponse<IssueDto.ListResponse>> getIssues(
		@Valid IssueDto.ListQueryRequest request,
		@CursorParam CursorDto.CursorData cursor) {

		log.info(request.toString());

		IssueDto.ListResponse response = issueService.findIssues(request, cursor);

		log.info("조건에 부합하는 이슈 개수= {}, 다음 페이지 존재= {}",
			response.totalCount(), response.cursor().hasNext());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/v1/issues/count")
	public ResponseEntity<ApiResponse<IssueDto.CountResponse>> getIssuesCount(
		@Valid IssueDto.CountQueryRequest request) {

		log.info(request.toString());

		IssueDto.CountResponse response = issueService.countIssues(request);

		log.info(response.toString());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
