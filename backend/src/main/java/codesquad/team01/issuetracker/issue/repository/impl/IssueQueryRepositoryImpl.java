package codesquad.team01.issuetracker.issue.repository.impl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.exception.IssueDeletionException;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.issue.exception.IssueCreationException;
import codesquad.team01.issuetracker.issue.exception.IssueNotFoundException;
import codesquad.team01.issuetracker.issue.exception.IssueUpdateException;
import codesquad.team01.issuetracker.issue.repository.IssueQueryBuilder;
import codesquad.team01.issuetracker.issue.repository.IssueQueryRepository;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class IssueQueryRepositoryImpl implements IssueQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final IssueQueryBuilder queryBuilder;

	private static final String SOFT_DELETE_ISSUE_QUERY = """
		UPDATE issue
		SET deleted_at = :now, updated_at = :now
		WHERE id = :issueId
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

	private static final String FIND_SPECIFIC_MILESTONE_ISSUE_COUNT = """
		SELECT
			i.milestone_id											AS milestoneId,
			SUM(CASE WHEN i.state = 'OPEN' THEN 1 ELSE 0 END)		AS openCount,
			SUM(CASE WHEN i.state = 'CLOSED' THEN 1 ELSE 0 END)		AS closedCount
		FROM issue i
		WHERE i.milestone_id IN (:milestoneIds)
		GROUP BY i.milestone_id
		""";

	private static final String UPDATE_ISSUE_STATES_BATCH_QUERY = """
		UPDATE issue
		SET state = :state,
			closed_at = CASE
				WHEN :state = 'CLOSED' THEN :now
				ELSE NULL
			END,
			updated_at = :now
		WHERE id IN (:issueIds)
		AND deleted_at IS NULL
		""";

	private static final String FIND_EXISTING_ISSUES_QUERY = """
		SELECT 
			i.id as issue_id,
			i.state as issue_state
		FROM issue i
		WHERE i.id IN (:issueIds)
		AND i.deleted_at IS NULL
		""";

	private static final String CREATE_ISSUE_QUERY = """
		INSERT INTO issue (title, content, state, writer_id, milestone_id, created_at, updated_at)
		VALUES (:title, :content, :state, :writerId, :milestoneId, :now, :now)
		""";

	private static final String FIND_CREATED_ISSUE_QUERY = """
		SELECT 
			i.id as issue_id,
			i.title as issue_title,
			i.content as issue_content,
			i.state as issue_state,
			i.created_at as issue_created_at,
			i.updated_at as issue_updated_at,
			i.closed_at as issue_closed_at,
			u.id as writer_id,
			u.username as writer_username,
			u.profile_image_url as writer_profile_image_url,
			m.id as milestone_id,
			m.title as milestone_title,
			m.due_date as milestone_due_date
		FROM issue i
		JOIN users u ON i.writer_id = u.id
		LEFT JOIN milestone m ON i.milestone_id = m.id
		WHERE i.id = :issueId
		AND i.deleted_at IS NULL
		""";

	private static final String INSERT_ISSUE_LABELS_BATCH_QUERY = """
		INSERT INTO issue_label (issue_id, label_id)
		VALUES (:issueId, :labelId)
		""";

	private static final String INSERT_ISSUE_ASSIGNEES_BATCH_QUERY = """
		INSERT INTO issue_assignee (issue_id, user_id)
		VALUES (:issueId, :userId)
		""";

	private static final String DELETE_ISSUE_LABELS_QUERY = """
		DELETE FROM issue_label WHERE issue_id = :issueId
		""";

	private static final String DELETE_ISSUE_ASSIGNEES_QUERY = """
		DELETE FROM issue_assignee WHERE issue_id = :issueId
		""";

	private static final String FIND_ISSUE_STATE_QUERY = """
		SELECT 
			i.state as state
		FROM issue i 
		WHERE i.id = :issueId 
		AND i.deleted_at IS NULL
		  """;
	private final RowMapper<IssueDto.BaseRow> issueRowMapper = (rs, rowNum) -> {

		Integer milestoneId = rs.getObject("milestone_id", Integer.class);
		String milestoneTitle = rs.getString("milestone_title");

		return IssueDto.BaseRow.builder()
			.issueId(rs.getInt("issue_id"))
			.issueTitle(rs.getString("issue_title"))
			.issueState(IssueState.fromStateStr(rs.getString("issue_state")))
			.issueCreatedAt(rs.getTimestamp("issue_created_at").toLocalDateTime())
			.issueUpdatedAt(rs.getTimestamp("issue_updated_at").toLocalDateTime())
			.writerId(rs.getInt("writer_id"))
			.writerUsername(rs.getString("writer_username"))
			.writerProfileImageUrl(rs.getString("writer_profile_image_url"))
			.milestoneId(milestoneId)
			.milestoneTitle(milestoneTitle)
			.build();
	};

	private final RowMapper<IssueDto.DetailBaseRow> detailIssueRowMapper =
		(rs, rowNum) -> {

			Timestamp closedAtTimestamp = rs.getTimestamp("issue_closed_at");
			LocalDateTime closedAt = closedAtTimestamp != null ?
				closedAtTimestamp.toLocalDateTime() : null;

			Integer milestoneId = rs.getObject("milestone_id", Integer.class);
			String milestoneTitle = rs.getString("milestone_title");
			Date milestoneDueDateSql = rs.getDate("milestone_due_date");
			LocalDate milestoneDueDate = milestoneDueDateSql != null ?
				milestoneDueDateSql.toLocalDate() : null;

			return IssueDto.DetailBaseRow.builder()
				.issueId(rs.getInt("issue_id"))
				.issueTitle(rs.getString("issue_title"))
				.issueContent(rs.getString("issue_content"))
				.issueState(IssueState.fromStateStr(rs.getString("issue_state")))
				.issueCreatedAt(rs.getTimestamp("issue_created_at").toLocalDateTime())
				.issueUpdatedAt(rs.getTimestamp("issue_updated_at").toLocalDateTime())
				.issueClosedAt(closedAt)
				.writerId(rs.getInt("writer_id"))
				.writerUsername(rs.getString("writer_username"))
				.writerProfileImageUrl(rs.getString("writer_profile_image_url"))
				.milestoneId(milestoneId)
				.milestoneTitle(milestoneTitle)
				.milestoneDueDate(milestoneDueDate)
				.build();
		};

	private final RowMapper<IssueDto.StateCountRow> stateCountRowMapper = (rs, rowNum) ->
		IssueDto.StateCountRow.builder()
			.state(rs.getString("state"))
			.count(rs.getInt("count"))
			.build();

	private final RowMapper<IssueDto.BatchIssueRow> batchIssueRowMapper = (rs, rowNum) ->
		IssueDto.BatchIssueRow.builder()
			.issueId(rs.getInt("issue_id"))
			.currentState(IssueState.fromStateStr(rs.getString("issue_state")))
			.build();

	public List<MilestoneDto.MilestoneIssueCountRow> countByMilestoneIds(List<Integer> milestoneIds) {
		StringBuilder sql = new StringBuilder(FIND_SPECIFIC_MILESTONE_ISSUE_COUNT);
		MapSqlParameterSource params = new MapSqlParameterSource("milestoneIds", milestoneIds);

		return jdbcTemplate.query(sql.toString(), params, milestoneIssueCountRowMapper);
	}

	private final RowMapper<MilestoneDto.MilestoneIssueCountRow> milestoneIssueCountRowMapper =
		(rs, rowNum) -> MilestoneDto.MilestoneIssueCountRow.builder()
			.milestoneId(rs.getInt("milestoneId"))
			.openCount(rs.getLong("openCount"))
			.closedCount(rs.getLong("closedCount"))
			.build();

	private final RowMapper<IssueState> stateRowMapper =
		(rs, rowNum) -> IssueState.fromStateStr(rs.getString("state"));

	@Override
	public List<IssueDto.BaseRow> findIssuesWithFilters(IssueDto.ListQueryParams queryParams,
		CursorDto.CursorData cursor) {
		IssueQueryBuilder.IssueListQueryInfo queryInfo =
			queryBuilder.buildListQuery(queryParams, cursor);

		return jdbcTemplate.query(queryInfo.query(), queryInfo.params(), issueRowMapper);
	}

	@Override
	public IssueDto.CountResponse countIssuesWithFilters(IssueDto.CountQueryParams queryParams) {

		IssueQueryBuilder.IssueCountQueryInfo queryInfo =
			queryBuilder.buildCountQuery(queryParams);

		List<IssueDto.StateCountRow> results = jdbcTemplate.query(
			queryInfo.query(), queryInfo.params(), stateCountRowMapper);

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

	@Override
	public int batchUpdateIssueStates(List<Integer> issueIds, IssueState targetState, LocalDateTime now) {
		if (issueIds.isEmpty()) {
			return 0;
		}

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("issueIds", issueIds);
		params.addValue("state", targetState.name());
		params.addValue("now", now);

		return jdbcTemplate.update(UPDATE_ISSUE_STATES_BATCH_QUERY, params);
	}

	@Override
	public List<IssueDto.BatchIssueRow> findExistingIssuesByIds(List<Integer> issueIds) {
		if (issueIds.isEmpty()) {
			return List.of();
		}
		MapSqlParameterSource params = new MapSqlParameterSource("issueIds", issueIds);
		return jdbcTemplate.query(FIND_EXISTING_ISSUES_QUERY, params, batchIssueRowMapper);
	}

	@Override
	public Integer createIssue(String title, String content, Integer writerId, Integer milestoneId, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("title", title);
		params.addValue("content", content);
		params.addValue("state", IssueState.OPEN.name());
		params.addValue("writerId", writerId);
		params.addValue("milestoneId", milestoneId);
		params.addValue("now", now);

		KeyHolder keyHolder = new GeneratedKeyHolder();

		try {
			int rowsAffected = jdbcTemplate.update(CREATE_ISSUE_QUERY, params, keyHolder);

			if (rowsAffected != 1) {
				throw new IssueCreationException("이슈 생성 실패. 영향 받은 행: " + rowsAffected);
			}

			Number generatedId = keyHolder.getKey();
			if (generatedId == null) {
				throw new IssueCreationException("생성된 이슈 ID를 가져올 수 없습니다.");
			}

			Integer issueId = generatedId.intValue();
			log.info("ID: {} 이슈 생성 완료: 제목={}", issueId, title);

			return issueId;
		} catch (DataAccessException e) {
			log.error("이슈 생성 중 db 오류 발생: {}", e.getMessage(), e);
			throw new IssueCreationException("데이터베이스 오류로 인해 이슈 생성 실패", e);
		}
	}

	@Override
	public IssueDto.DetailBaseRow findIssueById(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			List<IssueDto.DetailBaseRow> results =
				jdbcTemplate.query(FIND_CREATED_ISSUE_QUERY, params, detailIssueRowMapper);

			if (results.isEmpty()) {
				throw new IssueNotFoundException("생성된 이슈를 찾을 수 없습니다: " + issueId);
			}

			return results.get(0);
		} catch (DataAccessException e) {
			log.error("이슈 조회 중 데이터베이스 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new IssueNotFoundException("이슈 조회 중 오류 발생: " + issueId, e);
		}
	}

	@Override
	public void addLabelsToIssue(Integer issueId, List<Integer> labelIds) {
		if (labelIds.isEmpty()) {
			return;
		}

		Set<Integer> uniqueLabelIds = new LinkedHashSet<>(labelIds);

		List<MapSqlParameterSource> batchParams = uniqueLabelIds.stream()
			.map(labelId -> {
				MapSqlParameterSource params = new MapSqlParameterSource();
				params.addValue("issueId", issueId);
				params.addValue("labelId", labelId);
				return params;
			})
			.toList();

		try {
			int[] results = jdbcTemplate.batchUpdate(
				INSERT_ISSUE_LABELS_BATCH_QUERY,
				batchParams.toArray(new MapSqlParameterSource[0])
			);

			int successCount = (int)Arrays.stream(results).filter(result -> result > 0).count();
			log.debug("이슈 {}에 {}개 레이블 추가 완료 (성공: {}개)", issueId, labelIds.spliterator().estimateSize(), successCount);
		} catch (DataAccessException e) {
			log.error("이슈 레이블 추가 중 오류 발생: issueId={}, labelIds={}, error={}",
				issueId, labelIds, e.getMessage());
			throw new IssueCreationException("이슈 레이블 추가 중 오류 발생", e);
		}
	}

	@Override
	public void addAssigneesToIssue(Integer issueId, List<Integer> assigneeIds) {
		if (assigneeIds.isEmpty()) {
			return;
		}

		Set<Integer> uniqueAssigneeIds = new LinkedHashSet<>(assigneeIds);

		List<MapSqlParameterSource> batchParams = uniqueAssigneeIds.stream()
			.map(userId -> {
				MapSqlParameterSource params = new MapSqlParameterSource();
				params.addValue("issueId", issueId);
				params.addValue("userId", userId);
				return params;
			})
			.toList();

		try {
			int[] results = jdbcTemplate.batchUpdate(
				INSERT_ISSUE_ASSIGNEES_BATCH_QUERY,
				batchParams.toArray(new MapSqlParameterSource[0])
			);

			int successCount = (int)Arrays.stream(results).filter(result -> result > 0).count();
			log.debug("이슈 {}에 {}개 담당자 추가 완료 (성공: {}개)", issueId, assigneeIds.size(), successCount);

		} catch (DataAccessException e) {
			log.error("이슈 담당자 추가 중 오류 발생: issueId={}, assigneeIds={}, error={}",
				issueId, assigneeIds, e.getMessage());
			throw new IssueCreationException("이슈 담당자 추가 중 오류 발생", e);
		}
	}

	@Override
	public void removeLabelsFromIssue(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			int deletedCount = jdbcTemplate.update(DELETE_ISSUE_LABELS_QUERY, params);
			log.debug("이슈 {}의 모든 레이블 제거 완료: {}개", issueId, deletedCount);
		} catch (DataAccessException e) {
			log.error("이슈 레이블 제거 중 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new IssueCreationException("이슈 레이블 제거 중 오류 발생", e);
		}
	}

	@Override
	public void removeAssigneesFromIssue(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			int deletedCount = jdbcTemplate.update(DELETE_ISSUE_ASSIGNEES_QUERY, params);
			log.debug("이슈 {}의 모든 담당자 제거 완료: {}개", issueId, deletedCount);
		} catch (DataAccessException e) {
			log.error("이슈 담당자 제거 중 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new IssueCreationException("이슈 담당자 제거 중 오류 발생", e);
		}
	}

	@Override
	public IssueState findIssueStateByIssueId(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			return jdbcTemplate.queryForObject(FIND_ISSUE_STATE_QUERY, params, stateRowMapper);

		} catch (EmptyResultDataAccessException e) {
			throw new IssueNotFoundException("존재하지 않는 이슈입니다: " + issueId);
		} catch (DataAccessException e) {
			log.error("이슈 상태 및 작성자 조회 중 데이터베이스 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new IssueUpdateException("이슈 정보 조회 중 오류가 발생했습니다.", e);
		}
	}

	@Override
	public void updateIssue(Integer issueId, IssueDto.UpdateQueryParams queryParams, LocalDateTime now) {
		IssueQueryBuilder.IssueUpdateInfo updateInfo = queryBuilder.buildUpdateInfo(issueId, queryParams, now);

		try {
			if (updateInfo.baseQuery() != null) {
				int updatedRows = jdbcTemplate.update(updateInfo.baseQuery(), updateInfo.params());
				if (updatedRows == 0) {
					throw new IssueNotFoundException("이슈를 찾을 수 없거나 삭제된 이슈: " + issueId);
				}
				log.info("이슈 기본 필드 업데이트 완료: issueId={}", issueId);
			}
		} catch (DataAccessException e) {
			log.error("이슈 수정 중 데이터베이스 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new IssueUpdateException("이슈 수정 중 오류 발생", e);
		}
	}

	@Override
	public void deleteIssue(Integer issueId, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("issueId", issueId);
		params.addValue("now", now);

		try {
			int updatedRows = jdbcTemplate.update(SOFT_DELETE_ISSUE_QUERY, params);
			if (updatedRows == 0) {
				throw new IssueNotFoundException("이슈를 찾을 수 없거나 이미 삭제된 이슈: " + issueId);
			}
			log.info("이슈 삭제 완료: issueId={}", issueId);
		} catch (DataAccessException e) {
			log.error("이슈 삭제 중 데이터베이스 오류 발생: issueId, error={}", issueId, e.getMessage());
			throw new IssueDeletionException("이슈 삭제 중 오류 발생", e);
		}
	}
}
