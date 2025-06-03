package codesquad.team01.issuetracker.milestone.repository.impl;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.repository.MilestoneQueryRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class MilestoneQueryRepositoryImpl implements MilestoneQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FILTER_MILESTONE_LIST_QUERY = """
		SELECT 
			m.id as milestone_id,
			m.title as milestone_title
		FROM milestone m 
		WHERE m.deleted_at IS NULL
		ORDER BY m.title ASC
		""";

	private final RowMapper<MilestoneDto.MilestoneFilterResponse> filterMilestoneListItemResponseRowMapper =
		(rs, rowNum) -> MilestoneDto.MilestoneFilterResponse.builder()
			.id(rs.getInt("milestone_id"))
			.title(rs.getString("milestone_title"))
			.build();

	@Override
	public List<MilestoneDto.MilestoneFilterResponse> findMilestonesForFilter() {
		return jdbcTemplate.query(FILTER_MILESTONE_LIST_QUERY, filterMilestoneListItemResponseRowMapper);
	}
}
