package codesquad.team01.issuetracker.issue.repository;

import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.issue.domain.Issue;

public interface IssueRepository extends CrudRepository<Issue, Integer>, IssueQueryRepository {

}
