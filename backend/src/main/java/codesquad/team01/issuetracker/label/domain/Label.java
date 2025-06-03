package codesquad.team01.issuetracker.label.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import codesquad.team01.issuetracker.common.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("label")
public class Label extends BaseEntity {

	@Id
	private Integer id;

	private String name;

	private String description;

	private String color;

	private String textColor;

	@Builder
	private Label(String name, String description, String color, String textColor) {
		this.name = name;
		this.description = description;
		this.color = color;
		this.textColor = textColor;
	}
}
