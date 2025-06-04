package codesquad.team01.issuetracker.milestone.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.milestone.domain.MilestoneState;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.service.MilestoneService;
import jakarta.validation.Valid;
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

	@GetMapping("/v1/milestones")
	public ResponseEntity<ApiResponse<MilestoneDto.ListResponse>> getMilestones(@RequestParam("state") String state) {
		MilestoneState requiredState = MilestoneState.fromStateStr(state);
		MilestoneDto.ListResponse listResponse = milestoneService.getMilestones(requiredState);
		return ResponseEntity.ok(ApiResponse.success(listResponse));
	}

	@PostMapping("/v1/milestones")
	public ResponseEntity<ApiResponse<MilestoneDto.MilestoneCreateResponse>> createMilestone(
		@Valid @RequestBody MilestoneDto.MilestoneCreateRequest request) {
		MilestoneDto.MilestoneCreateResponse response = milestoneService.createMilestone(request);
		log.info("마일스톤 생성 완료: title={}", response.title());
		URI location = URI.create("/api/v1/milestones/" + response.id());
		return ResponseEntity.created(location).body(ApiResponse.success(response));
	}

	@PutMapping("/v1/milestones/{id}")
	public ResponseEntity<ApiResponse<MilestoneDto.MilestoneUpdateResponse>> updateMilestone(
		@PathVariable("id") int id,
		@Valid @RequestBody MilestoneDto.MilestoneUpdateRequest request) {
		MilestoneDto.MilestoneUpdateResponse response = milestoneService.updateMilestone(id, request);
		log.info("마일스톤 수정 완료: title={}", response.title());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@DeleteMapping("/v1/milestones/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteMilestone(@PathVariable("id") int id) {
		milestoneService.deleteMilestone(id);
		log.info("마일스톤 삭제 완료: id={}", id);
		return ResponseEntity.ok(ApiResponse.success(null));
	}
}
