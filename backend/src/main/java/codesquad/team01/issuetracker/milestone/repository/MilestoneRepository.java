package codesquad.team01.issuetracker.milestone.repository;

import org.springframework.data.repository.CrudRepository;

import codesquad.team01.issuetracker.milestone.domain.Milestone;

public interface MilestoneRepository extends CrudRepository<Milestone, Integer>, MilestoneQueryRepository {
}
