package codesquad.team01.issuetracker.user.repository.impl;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.user.dto.UserDto;
import codesquad.team01.issuetracker.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND_VALID_USER_IDS_QUERY = """
		SELECT id FROM users 
		WHERE id IN (:userIds) 
		AND deleted_at IS NULL
		""";

	private static final String SINGLE_ISSUE_ASSIGNEE_QUERY = """
		  	SELECT
		  		u.id as assignee_id,
		  		u.profile_image_url as assignee_profile_image_url,
		  		u.username as assignee_username
		  	FROM issue_assignee ia
		  	JOIN users u ON ia.user_id = u.id
		  	WHERE ia.issue_id = :issueId
		  	AND u.deleted_at IS NULL
		  	ORDER BY u.username ASC
		""";

	private static final String ASSIGNEE_QUERY = """
		    SELECT 
		        ia.issue_id,
		        u.id as assignee_id,
		        u.profile_image_url as assignee_profile_image_url
		    FROM issue_assignee ia
		    JOIN users u ON ia.user_id = u.id
		    WHERE ia.issue_id IN (:issueIds)
		    AND u.deleted_at IS NULL
		""";

	private static final String FILTER_USER_LIST_QUERY = """
			SELECT
				u.id as users_id,
				u.username as users_username,
				u.profile_image_url as users_profile_image_url
			FROM users u
			WHERE u.deleted_at IS NULL
			ORDER BY u.username ASC
		""";

	private final RowMapper<UserDto.IssueAssigneeRow> assigneeRowMapper = (rs, rowNum) -> UserDto.IssueAssigneeRow.builder()
		.issueId(rs.getInt("issue_id"))
		.assigneeId(rs.getInt("assignee_id"))
		.assigneeProfileImageUrl(rs.getString("assignee_profile_image_url"))
		.build();

	private final RowMapper<UserDto.UserFilterRow> filterUserListItemResponseRowMapper =
		(rs, rowNum) -> UserDto.UserFilterRow.builder()
			.id(rs.getInt("users_id"))
			.username(rs.getString("users_username"))
			.profileImageUrl(rs.getString("users_profile_image_url"))
			.build();

	private final RowMapper<UserDto.IssueDetailAssigneeRow> assigneeDetailRowMapper =
		(rs, rowNum) -> UserDto.IssueDetailAssigneeRow.builder()
			.assigneeId(rs.getInt("assignee_id"))
			.assigneeUsername(rs.getString("assignee_username"))
			.assigneeProfileImageUrl(rs.getString("assignee_profile_image_url"))
			.build();

	@Override
	public List<UserDto.IssueAssigneeRow> findAssigneesByIssueIds(List<Integer> issueIds) {

		if (issueIds.isEmpty()) {
			return List.of();
		}
		MapSqlParameterSource params = new MapSqlParameterSource("issueIds", issueIds);
		return jdbcTemplate.query(ASSIGNEE_QUERY, params, assigneeRowMapper);
	}

	@Override
	public List<UserDto.UserFilterRow> findUsersForFilter() {
		return jdbcTemplate.query(FILTER_USER_LIST_QUERY, filterUserListItemResponseRowMapper);
	}

	@Override
	public List<UserDto.IssueDetailAssigneeRow> findAssigneesByIssueId(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);
		return jdbcTemplate.query(SINGLE_ISSUE_ASSIGNEE_QUERY, params, assigneeDetailRowMapper);
	}

	@Override
	public List<Integer> findValidUserIds(List<Integer> userIds) {
		if (userIds.isEmpty()) {
			return List.of();
		}

		MapSqlParameterSource params = new MapSqlParameterSource("userIds", userIds);

		try {
			return jdbcTemplate.queryForList(FIND_VALID_USER_IDS_QUERY, params, Integer.class);
		} catch (DataAccessException e) {
			log.warn("유효한 사용자 ID 조회 중 오류 발생: {}", e.getMessage());
			return List.of();
		}
	}
}
