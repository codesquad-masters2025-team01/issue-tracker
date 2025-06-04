package codesquad.team01.issuetracker.issue.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.issue.constants.IssueConstants;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class IssueQueryBuilder {

	public IssueUpdateInfo buildUpdateInfo(Integer issueId, IssueDto.UpdateQueryParams queryParams, LocalDateTime now) {
		List<String> setClauses = new ArrayList<>();
		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("issueId", issueId);
		params.addValue("now", now);

		if (queryParams.isUpdatingTitle()) {
			setClauses.add("title = :title");
			params.addValue("title", queryParams.title());
		}

		if (queryParams.isUpdatingContent()) {
			setClauses.add("content = :content");
			params.addValue("content", queryParams.content());
		}

		if (queryParams.isUpdatingMilestone()) {
			setClauses.add("milestone_id = :milestoneId");
			params.addValue("milestoneId", queryParams.milestoneId());
		}

		if (queryParams.isUpdatingState()) {
			setClauses.add("state = :state");
			setClauses.add("closed_at = CASE WHEN :state = 'CLOSED' THEN :now ELSE NULL END");
			params.addValue("state", queryParams.action().name());
		}

		if (!setClauses.isEmpty()) {
			setClauses.add("updated_at = :now");
		}

		String baseQuery = setClauses.isEmpty() ? null :
			String.format("UPDATE issue SET %s WHERE id = :issueId AND deleted_at IS NULL",
				String.join(", ", setClauses));

		return IssueUpdateInfo.builder()
			.baseQuery(baseQuery)
			.params(params)
			.needsLabelUpdate(queryParams.isUpdatingLabels())
			.needsAssigneeUpdate(queryParams.isUpdatingAssignees())
			.labelIds(queryParams.labelIds())
			.assigneeIds(queryParams.assigneeIds())
			.build();
	}

	public IssueListQueryInfo buildListQuery(IssueDto.ListQueryParams queryParams, CursorDto.CursorData cursor) {
		StringBuilder baseQuery = new StringBuilder("""
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
			""");

		// filter=commented인 경우 comment 테이블과 조인 추가
		if ("commented".equals(queryParams.filter()) && queryParams.commentedUserId() != null) {
			baseQuery.append("""
				JOIN comment c ON i.id = c.issue_id 
				AND c.writer_id = :commentedUserId 
				AND c.deleted_at IS NULL
				""");
		}

		baseQuery.append("""
			WHERE i.deleted_at IS NULL
			AND i.state = :state
			""");

		MapSqlParameterSource params = new MapSqlParameterSource();
		List<String> conditions = new ArrayList<>();

		// 공통 필터 조건 추가
		addCommonFilterConditions(queryParams.writerId(), queryParams.milestoneId(), queryParams.labelIds(),
			queryParams.assigneeIds(), conditions, params);

		// commented 필터 처리
		if ("commented".equals(queryParams.filter()) && queryParams.commentedUserId() != null) {
			params.addValue("commentedUserId", queryParams.commentedUserId());
		}

		// 상태
		params.addValue("state", queryParams.state().name());

		// 커서
		if (cursor != null) {
			conditions.add("""
				(i.created_at < :cursorCreatedAt
				OR (i.created_at = :cursorCreatedAt AND i.id < :cursorId))
				""");
			params.addValue("cursorCreatedAt", cursor.getCreatedAt());
			params.addValue("cursorId", cursor.getId());
		}

		// AND
		if (!conditions.isEmpty()) {
			baseQuery.append(" AND ").append(String.join(" AND ", conditions));
		}

		// filter=commented인 경우 중복 제거를 위해 GROUP BY 추가
		if ("commented".equals(queryParams.filter()) && queryParams.commentedUserId() != null) {
			baseQuery.append(
				" GROUP BY i.id, i.title, i.state, i.created_at, i.updated_at, u.id, u.username, u.profile_image_url, m.id, m.title");
		}

		// 정렬
		baseQuery.append(" ORDER BY i.created_at DESC, i.id DESC");

		// 제한
		baseQuery.append(" LIMIT :pageSize");
		params.addValue("pageSize", IssueConstants.PAGE_SIZE + 1);

		return IssueListQueryInfo.builder()
			.query(baseQuery.toString())
			.params(params)
			.build();
	}

	public IssueCountQueryInfo buildCountQuery(IssueDto.CountQueryParams queryParams) {
		StringBuilder baseQuery = new StringBuilder("""
			SELECT 
				i.state,
				COUNT(*) as count
			FROM issue i
			WHERE i.deleted_at IS NULL
			""");

		MapSqlParameterSource params = new MapSqlParameterSource();
		List<String> conditions = new ArrayList<>();

		addCommonFilterConditions(queryParams.writerId(), queryParams.milestoneId(), queryParams.labelIds(),
			queryParams.assigneeIds(), conditions, params);

		// AND
		if (!conditions.isEmpty()) {
			baseQuery.append(" AND ").append(String.join(" AND ", conditions));
		}

		// GROUP BY
		baseQuery.append(" GROUP BY i.state");

		return IssueCountQueryInfo.builder()
			.query(baseQuery.toString())
			.params(params)
			.build();
	}

	private void addCommonFilterConditions(
		Integer writerId, Integer milestoneId, List<Integer> labelIds, List<Integer> assigneeIds,
		List<String> conditions, MapSqlParameterSource params) {

		// 작성자
		if (writerId != null) {
			conditions.add("i.writer_id = :writerId");
			params.addValue("writerId", writerId);
		}

		// 마일스톤
		if (milestoneId != null) {
			conditions.add("i.milestone_id = :milestoneId");
			params.addValue("milestoneId", milestoneId);
		}

		// 레이블
		addLabelFilterConditions(labelIds, conditions, params);

		// 담당자
		addAssigneeFilterConditions(assigneeIds, conditions, params);
	}

	private void addLabelFilterConditions(List<Integer> labelIds, List<String> conditions,
		MapSqlParameterSource params) {
		if (labelIds != null && !labelIds.isEmpty()) {
			for (int i = 0; i < labelIds.size(); i++) {
				conditions.add(String.format("""
					EXISTS (
					    SELECT 1 FROM issue_label il%d 
					    WHERE il%d.issue_id = i.id 
					    AND il%d.label_id = :labelId%d
					)""", i, i, i, i));
				params.addValue("labelId" + i, labelIds.get(i));
			}
		}
	}

	private void addAssigneeFilterConditions(List<Integer> assigneeIds, List<String> conditions,
		MapSqlParameterSource params) {
		if (assigneeIds != null && !assigneeIds.isEmpty()) {
			for (int i = 0; i < assigneeIds.size(); i++) {
				conditions.add(String.format("""
					EXISTS (
					    SELECT 1 FROM issue_assignee ia%d 
					    WHERE ia%d.issue_id = i.id 
					    AND ia%d.user_id = :assigneeId%d
					)""", i, i, i, i));
				params.addValue("assigneeId" + i, assigneeIds.get(i));
			}
		}
	}

	@Builder
	public record IssueListQueryInfo(
		String query,
		MapSqlParameterSource params
	) {
	}

	@Builder
	public record IssueCountQueryInfo(
		String query,
		MapSqlParameterSource params
	) {
	}

	@Builder
	public record IssueUpdateInfo(
		String baseQuery,
		MapSqlParameterSource params,
		boolean needsLabelUpdate,
		boolean needsAssigneeUpdate,
		List<Integer> labelIds,
		List<Integer> assigneeIds
	) {
	}
}
