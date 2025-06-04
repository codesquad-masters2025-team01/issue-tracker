package codesquad.team01.issuetracker.milestone.repository.impl;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.repository.MilestoneQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

	private static final String CHECK_MILESTONE_EXISTS_QUERY = """
		SELECT COUNT(*) > 0 FROM milestone 
		WHERE id = :milestoneId 
		AND deleted_at IS NULL
		""";

	private static final String FIND_ISSUE_COUNTS_BY_MILESTONE_ID_QUERY = """
		SELECT 
			i.state,
			COUNT(*) as count
		FROM issue i
		WHERE i.milestone_id = :milestoneId
		AND i.deleted_at IS NULL
		GROUP BY i.state
		""";

	private final RowMapper<MilestoneDto.MilestoneFilterResponse> filterMilestoneListItemResponseRowMapper =
		(rs, rowNum) -> MilestoneDto.MilestoneFilterResponse.builder()
			.id(rs.getInt("milestone_id"))
			.title(rs.getString("milestone_title"))
			.build();

	private final RowMapper<MilestoneDto.MilestoneIssueDetailCountRow> milestoneIssueCountRowMapper =
		(rs, rowNum) -> MilestoneDto.MilestoneIssueDetailCountRow.builder()
			.state(rs.getString("state"))
			.count(rs.getInt("count"))
			.build();

	@Override
	public List<MilestoneDto.MilestoneFilterResponse> findMilestonesForFilter() {
		return jdbcTemplate.query(FILTER_MILESTONE_LIST_QUERY, filterMilestoneListItemResponseRowMapper);
	}

	@Override
	public boolean existsMilestone(Integer milestoneId) {
		if (milestoneId == null) {
			return true;
		}

		MapSqlParameterSource params = new MapSqlParameterSource("milestoneId", milestoneId);

		try {
			Boolean result = jdbcTemplate.queryForObject(CHECK_MILESTONE_EXISTS_QUERY, params, Boolean.class);
			return result != null && result;
		} catch (DataAccessException e) {
			log.warn("마일스톤 존재 여부 확인 중 오류 발생: milestoneId={}, error={}", milestoneId, e.getMessage());
			return false;
		}
	}

	@Override
	public List<MilestoneDto.MilestoneIssueDetailCountRow> findIssueCountsByMilestoneId(Integer milestoneId) {
		MapSqlParameterSource params = new MapSqlParameterSource("milestoneId", milestoneId);

		try {
			return jdbcTemplate.query(FIND_ISSUE_COUNTS_BY_MILESTONE_ID_QUERY, params, milestoneIssueCountRowMapper);
		} catch (DataAccessException e) {
			log.warn("마일스톤 이슈 개수 조회 중 오류 발생: milestoneId={}, error={}", milestoneId, e.getMessage());
			return List.of();
		}
	}
}
