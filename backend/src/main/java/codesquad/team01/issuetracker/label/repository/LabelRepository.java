package codesquad.team01.issuetracker.label.repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.label.domain.Label;
import codesquad.team01.issuetracker.label.dto.LabelDto;

public interface LabelRepository extends CrudRepository<Label, Integer>, LabelQueryRepository {
	@Query("""
		SELECT 
			id,
			name,
			description,
			color,
			text_color
		FROM label
		WHERE deleted_at IS NULL
		""")
	List<LabelDto.LabelRow> findLabels();

	boolean existsByName(String name);
}
