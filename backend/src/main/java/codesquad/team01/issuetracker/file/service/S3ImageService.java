package codesquad.team01.issuetracker.file.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import codesquad.team01.issuetracker.common.config.ImageUploadProperties;
import codesquad.team01.issuetracker.file.dto.FileDto;
import codesquad.team01.issuetracker.file.exception.ImageUploadException;
import codesquad.team01.issuetracker.file.exception.ImageValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ImageService {

	private final S3Client s3Client;
	private final ImageUploadProperties imageUploadProperties;

	@Value("${spring.cloud.aws.s3.bucket}")
	private String bucketName;

	public FileDto.UploadResponse uploadImage(MultipartFile file) {
		validateFile(file);

		FileDto.ImageInfo imageInfo = createImageInfo(file);

		uploadTos3(file, imageInfo);

		log.info("이미지 업로드 완료: fileName={}, size={}, url={}",
			imageInfo.originalName(), imageInfo.size(), imageInfo.imageUrl());

		return FileDto.UploadResponse.builder()
			.imageUrl(imageInfo.imageUrl())
			.fileName(imageInfo.originalName())
			.fileSize(imageInfo.size())
			.contentType(imageInfo.contentType())
			.build();
	}

	private void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ImageValidationException("업로드할 파일이 없습니다.");
		}

		// 파일 크기 검증
		if (file.getSize() > imageUploadProperties.getMaxSize()) {
			throw new ImageValidationException(
				String.format("파일 크기가 허용 범위 초과입니다. 최대 허용 범위=%s바이트", imageUploadProperties.getMaxSize())
			);
		}

		// 확장자 검증
		String originalFilename = file.getOriginalFilename();
		if (!StringUtils.hasText(originalFilename)) {
			throw new ImageValidationException("파일명이 유효하지 않습니다.");
		}

		String extension = getFileExtension(originalFilename).toLowerCase();
		String[] allowedExtensions = imageUploadProperties.getAllowedExtensionsArray();

		boolean isAllowed = Arrays.stream(allowedExtensions)
			.anyMatch(ext -> ext.trim().toLowerCase().equals(extension));

		if (!isAllowed) {
			throw new ImageValidationException(
				String.format("지원하지 않는 파일 형식, 허용된 형식: %s", String.join(", ", allowedExtensions))
			);
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new ImageValidationException("이미지 파일만 업로드 가능합니다.");
		}
	}

	private FileDto.ImageInfo createImageInfo(MultipartFile file) {
		String originalName = file.getOriginalFilename();
		String extension = getFileExtension(originalName);

		// images/yyyy/mm/dd/uuid.jpg
		String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		String storedName = String.format("images/%s/%s.%s",
			datePath, UUID.randomUUID().toString(), extension);

		String imageUrl = String.format("%s/%s", imageUploadProperties.getBaseUrl(), storedName);

		return FileDto.ImageInfo.builder()
			.originalName(originalName)
			.storedName(storedName)
			.contentType(file.getContentType())
			.size(file.getSize())
			.s3Key(storedName)
			.imageUrl(imageUrl)
			.build();

	}

	private void uploadTos3(MultipartFile file, FileDto.ImageInfo imageInfo) {
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(imageInfo.s3Key())
				.contentType(imageInfo.contentType())
				.contentLength(imageInfo.size())
				.build();

			s3Client.putObject(putObjectRequest,
				RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

		} catch (S3Exception e) {
			log.error("S3 업로드 실패: {}", e.getMessage(), e);
			throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
		} catch (IOException e) {
			log.error("파일 읽기 실패: {}", e.getMessage(), e);
			throw new ImageUploadException("파일을 읽는 중 오류가 발생했습니다.", e);
		}
	}

	private String getFileExtension(String filename) {
		if (!StringUtils.hasText(filename)) {
			return "";
		}
		int lastDotIndex = filename.lastIndexOf('.');
		return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex + 1);
	}
}
