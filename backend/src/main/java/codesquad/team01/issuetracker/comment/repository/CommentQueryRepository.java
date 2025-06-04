package codesquad.team01.issuetracker.comment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import codesquad.team01.issuetracker.comment.dto.CommentDto;
import codesquad.team01.issuetracker.common.dto.CursorDto;

public interface CommentQueryRepository {

	List<CommentDto.CommentRow> findCommentsByIssueId(Integer issueId);

	List<CommentDto.CommentRow> findCommentsByIssueIdWithCursor(Integer issueId, CursorDto.CursorData cursor,
		int pageSize);

	Optional<CommentDto.CommentDetails> findCommentById(Integer commentId);

	Integer createComment(String content, Integer writerId, Integer issueId, LocalDateTime now);

	void updateComment(Integer commentId, String content, LocalDateTime now);

	void deleteComment(Integer commentId, LocalDateTime now);

	int countCommentsByIssueId(Integer issueId);

	void deleteCommentsByIssueId(Integer issueId, LocalDateTime now);
}
