package codesquad.team01.issuetracker.file.dto;

import lombok.Builder;

public class FileDto {

	private FileDto() {
	}

	@Builder
	public record UploadResponse(
		String imageUrl,
		String fileName,
		long fileSize,
		String contentType
	) {
	}

	@Builder
	public record ImageInfo(
		String originalName,
		String storedName,
		String contentType,
		long size,
		String s3Key,
		String imageUrl
	) {
	}
}
