package codesquad.team01.issuetracker.issue.repository.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.constants.IssueConstants;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.issue.repository.IssueQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class IssueQueryRepositoryImpl implements IssueQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	private static final String BASE_ISSUE_QUERY = """
		SELECT
		    i.id as issue_id,
		    i.title as issue_title,
		    i.state as issue_state,
		    i.created_at as issue_created_at,
		    i.updated_at as issue_updated_at,
		    u.id as writer_id,
		    u.username as writer_username,
		    u.profile_image_url as writer_profile_image_url,
		    m.id as milestone_id,
		    m.title as milestone_title
		FROM issue i
		JOIN users u ON i.writer_id = u.id
		LEFT JOIN milestone m ON i.milestone_id = m.id
		WHERE i.deleted_at IS NULL
		    AND i.state = :state 
		""";

	private static final String BASE_COUNT_ISSUES_QUERY = """
		SELECT 
			i.state,
			COUNT(*) as count
		FROM issue i
		WHERE i.deleted_at IS NULL 
		""";

	private final RowMapper<IssueDto.BaseRow> issueRowMapper = (rs, rowNum) -> IssueDto.BaseRow.builder()
		.issueId(rs.getInt("issue_id"))

		.issueTitle(rs.getString("issue_title"))
		.issueState(IssueState.fromStateStr(rs.getString("issue_state")))
		// enum 변환 - spring 내에서는 enum 사용이 타입 검증에도 맞을 것 같아서
		// 요청 받을 때, db 조회해올 때 빼고는 spring 내에선 enum으로 변환해서 관리 - 이게 맞는 건지는 잘 모르겠음
		// 그러기엔 IssueAssembler에서 dto 재활용을 위해 textColor를 미리 String으로 변환하긴 함.
		// 그럼 위에 있는 나름의 논리가 맞지 않음
		.issueCreatedAt(rs.getTimestamp("issue_created_at").toLocalDateTime())
		.issueUpdatedAt(rs.getTimestamp("issue_updated_at").toLocalDateTime())
		.writerId(rs.getInt("writer_id"))
		.writerUsername(rs.getString("writer_username"))
		.writerProfileImageUrl(rs.getString("writer_profile_image_url"))
		.milestoneId(rs.getInt("milestone_id"))
		.milestoneTitle(rs.getString("milestone_title"))
		.build();

	@Override
	public List<IssueDto.BaseRow> findIssuesWithFilters(
		IssueState state, Integer writerId, Integer milestoneId,
		List<Integer> labelIds, List<Integer> assigneeIds, CursorDto.CursorData cursor) {

		StringBuilder sql = new StringBuilder(BASE_ISSUE_QUERY);
		MapSqlParameterSource params = new MapSqlParameterSource();

		// state
		params.addValue("state", state.name());

		appendFilterConditions(sql, params, writerId, milestoneId, labelIds, assigneeIds);

		// 무한스크롤 커서
		if (cursor != null) {
			sql.append("""
				AND (i.created_at < :cursorCreatedAt 
				     OR (i.created_at = :cursorCreatedAt AND i.id < :cursorId))
				""");
			params.addValue("cursorCreatedAt", cursor.getCreatedAt());
			params.addValue("cursorId", cursor.getId());
		}

		// 정렬: 생성일자 내림차순 (최신순)
		sql.append(" ORDER BY i.created_at DESC, i.id DESC");

		sql.append(" LIMIT :pageSize");
		params.addValue("pageSize", IssueConstants.PAGE_SIZE + 1); // 다음 페이지 존재 여부 확인을 위해 +1

		return jdbcTemplate.query(sql.toString(), params, issueRowMapper);
	}

	private final RowMapper<IssueDto.StateCountRow> stateCountRowMapper = (rs, rowNum) ->
		IssueDto.StateCountRow.builder()
			.state(rs.getString("state"))
			.count(rs.getInt("count"))
			.build();

	@Override
	public IssueDto.CountResponse countIssuesWithFilters(Integer writerId, Integer milestoneId,
		List<Integer> labelIds, List<Integer> assigneeIds) {

		StringBuilder sql = new StringBuilder(BASE_COUNT_ISSUES_QUERY);
		MapSqlParameterSource params = new MapSqlParameterSource();

		appendFilterConditions(sql, params, writerId, milestoneId, labelIds, assigneeIds);

		sql.append(" GROUP BY i.state");

		List<IssueDto.StateCountRow> results = jdbcTemplate.query(sql.toString(), params, stateCountRowMapper);

		Map<IssueState, Integer> countByState = results.stream()
			.collect(Collectors.toMap(
				row -> IssueState.fromStateStr(row.state()),
				IssueDto.StateCountRow::count
			));

		return IssueDto.CountResponse.builder()
			.open(countByState.getOrDefault(IssueState.OPEN, 0))
			.closed(countByState.getOrDefault(IssueState.CLOSED, 0))
			.build();
	}

	private void appendFilterConditions(StringBuilder sql, MapSqlParameterSource params,
		Integer writerId, Integer milestoneId,
		List<Integer> labelIds, List<Integer> assigneeIds) {

		// 작성자
		if (writerId != null) {
			sql.append(" AND i.writer_id = :writerId");
			params.addValue("writerId", writerId);
		}

		// 마일스톤
		if (milestoneId != null) {
			sql.append(" AND i.milestone_id = :milestoneId");
			params.addValue("milestoneId", milestoneId);
		}

		// 레이블
		if (labelIds != null && !labelIds.isEmpty()) {
			for (int i = 0; i < labelIds.size(); i++) {
				sql.append("""
					AND EXISTS (
					   SELECT 1 FROM issue_label il2 
					   WHERE il2.issue_id = i.id 
					   AND il2.label_id = :labelId""").append(i).append(")\n");
				params.addValue("labelId" + i, labelIds.get(i));
			}
		}

		// 담당자
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			for (int i = 0; i < assigneeIds.size(); i++) {
				sql.append("""
					AND EXISTS (
					   SELECT 1 FROM issue_assignee ia2 
					   WHERE ia2.issue_id = i.id 
					   AND ia2.user_id = :assigneeId""").append(i).append(")\n");
				params.addValue("assigneeId" + i, assigneeIds.get(i));
			}
		}
	}
}
