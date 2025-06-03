package codesquad.team01.issuetracker.common.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;

@Getter
public abstract class BaseEntity {

	@CreatedDate
	private LocalDateTime createdAt;

	@LastModifiedDate
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void delete() {
		this.deletedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
	}

	public void restore() {
		this.deletedAt = null;
	}
}
