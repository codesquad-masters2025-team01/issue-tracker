package codesquad.team01.issuetracker.file.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import codesquad.team01.issuetracker.common.dto.ApiResponse;
import codesquad.team01.issuetracker.file.dto.FileDto;
import codesquad.team01.issuetracker.file.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class FileController {

	private final S3ImageService s3ImageService;

	@PostMapping("/v1/images")
	public ResponseEntity<ApiResponse<FileDto.UploadResponse>> uploadImage(
		@RequestParam("image") MultipartFile file) {
		log.info("이미지 업로드 요청: fileName={}, size={}, contentType={}",
			file.getOriginalFilename(), file.getSize(), file.getContentType());

		FileDto.UploadResponse response = s3ImageService.uploadImage(file);

		log.info("이미지 업로드 완료: url={}", response.imageUrl());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
