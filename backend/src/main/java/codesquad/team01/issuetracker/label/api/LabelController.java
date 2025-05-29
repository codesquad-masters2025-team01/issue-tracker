package codesquad.team01.issuetracker.label.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.label.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class LabelController {

	private final LabelService labelService;

	@GetMapping("/v1/labels")
	public ResponseEntity<ApiResponse<LabelDto.ListResponse>> getLabels() {
		LabelDto.ListResponse listResponse = labelService.getLabels();
		return ResponseEntity.ok(ApiResponse.success(listResponse));
	}

	@GetMapping("/v1/labels/filters")
	public ResponseEntity<ApiResponse<LabelDto.LabelFilterListResponse>> getLabelFilter() {
		LabelDto.LabelFilterListResponse response = labelService.findLabelsForFilter();
		log.info("레이블 목록 개수= {}", response.totalCount());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping("/v1/labels")
	public ResponseEntity<ApiResponse<LabelDto.LabelCreateResponse>> createLabel(
		@Valid @RequestBody LabelDto.LabelCreateRequest request) {
		LabelDto.LabelCreateResponse response = labelService.saveLabel(request);
		log.info("레이블 생성 완료: name={}", response.name());
		return ResponseEntity
			.created(URI.create("/api/v1/labels" + response.id()))
			.body(ApiResponse.success(response));
	}

	@PutMapping("/v1/labels/{id}")
	public ResponseEntity<ApiResponse<LabelDto.LabelUpdateResponse>> updateLabel(@PathVariable("id") int id,
		@Valid @RequestBody LabelDto.LabelUpdateRequest request) {
		LabelDto.LabelUpdateResponse response = labelService.updateLabel(id, request);
		log.info("레이블 수정 완료: name={}", response.name());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
