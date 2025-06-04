package codesquad.team01.issuetracker.comment.repository.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.comment.dto.CommentDto;
import codesquad.team01.issuetracker.comment.exception.CommentCreationException;
import codesquad.team01.issuetracker.comment.exception.CommentNotFoundException;
import codesquad.team01.issuetracker.comment.repository.CommentQueryRepository;
import codesquad.team01.issuetracker.common.dto.CursorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class CommentQueryRepositoryImpl implements CommentQueryRepository {
	private final NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND_COMMENTS_BY_ISSUE_ID_WITH_CURSOR_QUERY = """
		SELECT 
		    c.id as comment_id,
		    c.content as comment_content,
		    c.created_at as comment_created_at,
		    c.updated_at as comment_updated_at,
		    u.id as writer_id,
		    u.username as writer_username,
		    u.profile_image_url as writer_profile_image_url
		FROM comment c
		JOIN users u ON c.writer_id = u.id
		WHERE c.issue_id = :issueId
		AND c.deleted_at IS NULL
		AND (:cursorCreatedAt IS NULL OR c.created_at < :cursorCreatedAt OR 
		     (c.created_at = :cursorCreatedAt AND c.id < :cursorId))
		ORDER BY c.created_at DESC, c.id DESC
		LIMIT :pageSize
		""";

	private static final String FIND_COMMENTS_BY_ISSUE_ID_QUERY = """
		SELECT 
		    c.id as comment_id,
		    c.content as comment_content,
		    c.created_at as comment_created_at,
		    c.updated_at as comment_updated_at,
		    u.id as writer_id,
		    u.username as writer_username,
		    u.profile_image_url as writer_profile_image_url
		FROM comment c
		JOIN users u ON c.writer_id = u.id
		WHERE c.issue_id = :issueId
		AND c.deleted_at IS NULL
		ORDER BY c.created_at ASC
		""";

	private static final String FIND_COMMENT_BY_ID_QUERY = """
		SELECT 
		    c.id,
		    c.content,
		    c.created_at,
		    c.updated_at,
		    c.writer_id,
		    c.issue_id
		FROM comment c
		WHERE c.id = :commentId
		AND c.deleted_at IS NULL
		""";

	private static final String CREATE_COMMENT_QUERY = """
		INSERT INTO comment (content, writer_id, issue_id, created_at, updated_at)
		VALUES (:content, :writerId, :issueId, :now, :now)
		""";

	private static final String UPDATE_COMMENT_QUERY = """
		UPDATE comment 
		SET content = :content, updated_at = :now
		WHERE id = :commentId 
		AND deleted_at IS NULL
		""";

	private static final String DELETE_COMMENT_QUERY = """
		UPDATE comment 
		SET deleted_at = :now, updated_at = :now
		WHERE id = :commentId 
		AND deleted_at IS NULL
		""";

	private static final String COUNT_COMMENTS_BY_ISSUE_ID_QUERY = """
		SELECT COUNT(*) 
		FROM comment 
		WHERE issue_id = :issueId 
		AND deleted_at IS NULL
		""";

	private static final String DELETE_COMMENTS_BY_ISSUE_ID_QUERY = """
		UPDATE comment 
		SET deleted_at = :now, updated_at = :now
		WHERE issue_id = :issueId 
		AND deleted_at IS NULL
		""";

	private final RowMapper<CommentDto.CommentRow> commentRowMapper = (rs, rowNum) ->
		CommentDto.CommentRow.builder()
			.commentId(rs.getInt("comment_id"))
			.commentContent(rs.getString("comment_content"))
			.commentCreatedAt(rs.getTimestamp("comment_created_at").toLocalDateTime())
			.commentUpdatedAt(rs.getTimestamp("comment_updated_at").toLocalDateTime())
			.writerId(rs.getInt("writer_id"))
			.writerUsername(rs.getString("writer_username"))
			.writerProfileImageUrl(rs.getString("writer_profile_image_url"))
			.build();

	private final RowMapper<CommentDto.CommentDetails> commentDetailsMapper = (rs, rowNum) ->
		CommentDto.CommentDetails.builder()
			.id(rs.getInt("id"))
			.content(rs.getString("content"))
			.createdAt(rs.getTimestamp("created_at").toLocalDateTime())
			.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
			.writerId(rs.getInt("writer_id"))
			.issueId(rs.getInt("issue_id"))
			.build();

	@Override
	public List<CommentDto.CommentRow> findCommentsByIssueIdWithCursor(Integer issueId, CursorDto.CursorData cursor,
		int pageSize) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("issueId", issueId);
		params.addValue("pageSize", pageSize + 1); // hasNext 판단을 위해 +1

		if (cursor != null) {
			params.addValue("cursorCreatedAt", cursor.getCreatedAt());
			params.addValue("cursorId", cursor.getId());
		} else {
			params.addValue("cursorCreatedAt", null);
			params.addValue("cursorId", null);
		}

		try {
			return jdbcTemplate.query(FIND_COMMENTS_BY_ISSUE_ID_WITH_CURSOR_QUERY, params, commentRowMapper);
		} catch (DataAccessException e) {
			log.error("이슈 댓글 커서 조회 중 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new CommentNotFoundException("댓글 조회 중 오류 발생: " + issueId);
		}
	}

	@Override
	public List<CommentDto.CommentRow> findCommentsByIssueId(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			return jdbcTemplate.query(FIND_COMMENTS_BY_ISSUE_ID_QUERY, params, commentRowMapper);
		} catch (DataAccessException e) {
			log.error("이슈 댓글 조회 중 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new CommentNotFoundException("댓글 조회 중 오류 발생: " + issueId);
		}
	}

	@Override
	public Optional<CommentDto.CommentDetails> findCommentById(Integer commentId) {
		MapSqlParameterSource params = new MapSqlParameterSource("commentId", commentId);

		try {
			CommentDto.CommentDetails comment = jdbcTemplate.queryForObject(
				FIND_COMMENT_BY_ID_QUERY, params, commentDetailsMapper);
			return Optional.ofNullable(comment);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		} catch (DataAccessException e) {
			log.error("댓글 조회 중 오류 발생: commentId={}, error={}", commentId, e.getMessage());
			throw new CommentNotFoundException("댓글 조회 중 오류 발생: " + commentId);
		}
	}

	@Override
	public Integer createComment(String content, Integer writerId, Integer issueId, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("content", content);
		params.addValue("writerId", writerId);
		params.addValue("issueId", issueId);
		params.addValue("now", now);

		KeyHolder keyHolder = new GeneratedKeyHolder();

		try {
			int rowsAffected = jdbcTemplate.update(CREATE_COMMENT_QUERY, params, keyHolder);

			if (rowsAffected != 1) {
				throw new CommentCreationException("댓글 생성 실패. 영향 받은 행: " + rowsAffected);
			}

			Number generatedId = keyHolder.getKey();
			if (generatedId == null) {
				throw new CommentCreationException("생성된 댓글 ID를 가져올 수 없습니다.");
			}

			Integer commentId = generatedId.intValue();
			log.info("댓글 생성 완료: commentId={}, issueId={}", commentId, issueId);

			return commentId;
		} catch (DataAccessException e) {
			log.error("댓글 생성 중 db 오류 발생: {}", e.getMessage(), e);
			throw new CommentCreationException("데이터베이스 오류로 인해 댓글 생성 실패", e);
		}
	}

	@Override
	public void updateComment(Integer commentId, String content, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("commentId", commentId);
		params.addValue("content", content);
		params.addValue("now", now);

		try {
			int updatedRows = jdbcTemplate.update(UPDATE_COMMENT_QUERY, params);
			if (updatedRows == 0) {
				throw new CommentNotFoundException("댓글을 찾을 수 없거나 이미 삭제된 댓글: " + commentId);
			}
			log.info("댓글 수정 완료: commentId={}", commentId);
		} catch (DataAccessException e) {
			log.error("댓글 수정 중 데이터베이스 오류 발생: commentId={}, error={}", commentId, e.getMessage());
			throw new CommentCreationException("댓글 수정 중 오류 발생", e);
		}
	}

	@Override
	public void deleteComment(Integer commentId, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("commentId", commentId);
		params.addValue("now", now);

		try {
			int updatedRows = jdbcTemplate.update(DELETE_COMMENT_QUERY, params);
			if (updatedRows == 0) {
				throw new CommentNotFoundException("댓글을 찾을 수 없거나 이미 삭제된 댓글: " + commentId);
			}
			log.info("댓글 삭제 완료: commentId={}", commentId);
		} catch (DataAccessException e) {
			log.error("댓글 삭제 중 데이터베이스 오류 발생: commentId={}, error={}", commentId, e.getMessage());
			throw new CommentCreationException("댓글 삭제 중 오류 발생", e);
		}
	}

	@Override
	public int countCommentsByIssueId(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);

		try {
			Integer count = jdbcTemplate.queryForObject(COUNT_COMMENTS_BY_ISSUE_ID_QUERY, params, Integer.class);
			return count != null ? count : 0;
		} catch (DataAccessException e) {
			log.warn("댓글 개수 조회 중 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			return 0;
		}
	}

	@Override
	public void deleteCommentsByIssueId(Integer issueId, LocalDateTime now) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("issueId", issueId);
		params.addValue("now", now);

		try {
			int updatedRows = jdbcTemplate.update(DELETE_COMMENTS_BY_ISSUE_ID_QUERY, params);
			log.info("이슈의 모든 댓글 삭제 완료: issueId={}, deletedCount={}", issueId, updatedRows);
		} catch (DataAccessException e) {
			log.error("이슈 댓글들 삭제 중 데이터베이스 오류 발생: issueId={}, error={}", issueId, e.getMessage());
			throw new CommentCreationException("이슈 댓글들 삭제 중 오류 발생", e);
		}
	}
}
