package codesquad.team01.issuetracker.label.repository;

import java.util.List;
import java.util.Optional;

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
			text_color AS textColor
		FROM label
		WHERE deleted_at IS NULL
		""")
	List<LabelDto.LabelRow> findLabels();

	boolean existsByName(String name);

	boolean existsById(int id);

	Optional<Label> findById(int id);

	// name 같고 id 다른 레코드가 있으면 true 반환
	boolean existsByNameAndIdNot(String name, int id);
}
