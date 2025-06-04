package codesquad.team01.issuetracker.milestone.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.team01.issuetracker.common.exception.DuplicateMilestoneTitleException;
import codesquad.team01.issuetracker.common.exception.InvalidDateException;
import codesquad.team01.issuetracker.common.exception.MilestoneNotFoundException;
import codesquad.team01.issuetracker.issue.repository.IssueRepository;
import codesquad.team01.issuetracker.milestone.domain.Milestone;
import codesquad.team01.issuetracker.milestone.domain.MilestoneState;
import codesquad.team01.issuetracker.milestone.dto.MilestoneDto;
import codesquad.team01.issuetracker.milestone.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MilestoneService {

	private final MilestoneRepository milestoneRepository;
	private final IssueRepository issueRepository;

	public MilestoneDto.MilestoneFilterListResponse findMilestonesForFilter() {

		List<MilestoneDto.MilestoneFilterResponse> milestones =
			milestoneRepository.findMilestonesForFilter();

		return MilestoneDto.MilestoneFilterListResponse.builder()
			.totalCount(milestones.size())
			.milestones(milestones)
			.build();
	}

	public MilestoneDto.ListResponse getMilestones(MilestoneState state) {
		// state(open, close) 에 해당하는 마일스톤 목록 불러와서
		List<MilestoneDto.MilestoneRow> rows = milestoneRepository.findByState(state);

		// id 의 리스트만 뽑아낸다
		List<Integer> milestoneIds = rows.stream()
			.map(MilestoneDto.MilestoneRow::id)
			.toList();

		// id 리스트가 비었으면 빈 ListResponse 반환
		if (milestoneIds.isEmpty()) {
			return new MilestoneDto.ListResponse(0, List.of());
		}

		// id 의 리스트로 issueCounts 리스트 불러와서
		List<MilestoneDto.MilestoneIssueCountRow> issueCounts = issueRepository.countByMilestoneIds(milestoneIds);

		// <마일스톤 id, IssueCount> 맵 만든다
		Map<Integer, MilestoneDto.MilestoneIssueCountRow> countMap = issueCounts.stream()
			.collect(Collectors.toMap(
				MilestoneDto.MilestoneIssueCountRow::milestoneId,
				Function.identity()
			));

		// 맨위에서 불러온 rows 의 id 를 키 값으로 해서 countMap 에서 해당하는 issueCount 를 불러온다.
		// row 와 issueCount 로 MilestoneListItem 를 만들고 리스트로 합친다
		List<MilestoneDto.MilestoneListItem> items = rows.stream()
			.map(row -> {
				MilestoneDto.MilestoneIssueCountRow count = countMap.getOrDefault(row.id(),
					new MilestoneDto.MilestoneIssueCountRow(
						row.id(), 0L, 0L));
				return MilestoneDto.MilestoneListItem.from(row, count);
			})
			.toList();

		return new MilestoneDto.ListResponse(items.size(), items);
	}

	@Transactional
	public MilestoneDto.MilestoneCreateResponse createMilestone(MilestoneDto.MilestoneCreateRequest request) {
		LocalDate dueDate = parseDueDate(request.dueDate());
		String milestoneTitle = request.title();

		if (milestoneRepository.existsByTitle(milestoneTitle)) {
			throw new DuplicateMilestoneTitleException(milestoneTitle);
		}

		Milestone entity = Milestone.builder()
			.title(request.title())
			.description(request.description())
			.dueDate(dueDate)
			.state(MilestoneState.OPEN)
			.build();

		Milestone savedMilestone = milestoneRepository.save(entity);

		return MilestoneDto.MilestoneCreateResponse.from(savedMilestone);
	}

	private LocalDate parseDueDate(String inputDueDate) {
		if (inputDueDate == null || inputDueDate.isBlank()) {
			return null;
		}
		try {
			return LocalDate.parse(inputDueDate);
		} catch (DateTimeParseException e) {
			throw new InvalidDateException("잘못된 마감일 형식입니다. 연월일을 확인하세요.");
		}
	}

	@Transactional
	public MilestoneDto.MilestoneUpdateResponse updateMilestone(int id, MilestoneDto.MilestoneUpdateRequest request) {
		String newTitle = request.title();
		LocalDate newDueDate = parseDueDate(request.dueDate());
		MilestoneState newState = MilestoneState.fromStateStr(request.state());

		Milestone updateTarget = milestoneRepository.findById(id)
			.orElseThrow(() -> new MilestoneNotFoundException(id));

		if (milestoneRepository.existsByTitleAndIdNot(newTitle, id)) {
			throw new DuplicateMilestoneTitleException(newTitle);
		}

		updateTarget.update(newTitle, request.description(), newDueDate, newState);
		Milestone saved = milestoneRepository.save(updateTarget);

		return MilestoneDto.MilestoneUpdateResponse.from(saved);
	}

	@Transactional
	public void deleteMilestone(int id) {
		Milestone targetMilestone = milestoneRepository.findById(id)
			.orElseThrow(() -> new MilestoneNotFoundException(id));

		targetMilestone.delete();
		milestoneRepository.save(targetMilestone);
	}
}
