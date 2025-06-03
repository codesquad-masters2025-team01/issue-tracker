package codesquad.team01.issuetracker.user.repository.impl;

import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.user.dto.UserDto;
import codesquad.team01.issuetracker.user.repository.UserQueryRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

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
}
