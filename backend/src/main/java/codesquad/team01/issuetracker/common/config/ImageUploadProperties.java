package codesquad.team01.issuetracker.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "image.upload")
public class ImageUploadProperties {
	private String allowedExtensions = "jpg,jpeg,png,gif,webp";
	private long maxSize = 10485760L; // 10MB
	private String baseUrl;

	public String[] getAllowedExtensionsArray() {
		return allowedExtensions.split(",");
	}
}
