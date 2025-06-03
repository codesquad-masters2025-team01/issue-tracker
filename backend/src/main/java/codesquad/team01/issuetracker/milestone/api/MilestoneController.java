package codesquad.team01.issuetracker.milestone.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
public class MilestoneController {

	private final MilestoneService milestoneService;

	@GetMapping("/v1/milestones/filters")
	public ResponseEntity<ApiResponse<MilestoneDto.MilestoneFilterListResponse>> getMilestones() {
		MilestoneDto.MilestoneFilterListResponse response = milestoneService.findMilestonesForFilter();
		log.info("마일스톤 목록 개수= {}", response.totalCount());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
