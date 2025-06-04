package codesquad.team01.issuetracker.milestone.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import codesquad.team01.issuetracker.milestone.domain.Milestone;
import codesquad.team01.issuetracker.milestone.domain.MilestoneState;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;

public interface MilestoneRepository extends CrudRepository<Milestone, Integer>, MilestoneQueryRepository {

	@Query("""
		SELECT
			id,
			title,
			description,
			due_date AS dueDate,
			state
		FROM milestone
		WHERE deleted_at IS NULL
		AND state= :state
		""")
	List<MilestoneDto.MilestoneRow> findByState(@Param("state") MilestoneState state);

	boolean existsByTitle(String title);

	boolean existsByTitleAndIdNot(String title, int id);

	boolean existsById(int id);

	Optional<Milestone> findById(int id);
}
