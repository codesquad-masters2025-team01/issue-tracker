package codesquad.team01.issuetracker.comment.repository;

import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.comment.domain.Comment;

public interface CommentRepository extends CrudRepository<Comment, Integer>, CommentQueryRepository {
}
