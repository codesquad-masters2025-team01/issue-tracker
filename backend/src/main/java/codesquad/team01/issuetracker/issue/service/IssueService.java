package codesquad.team01.issuetracker.issue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.util.CursorEncoder;
import codesquad.team01.issuetracker.issue.constants.IssueConstants;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.issue.repository.IssueRepository;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.label.repository.LabelRepository;
import codesquad.team01.issuetracker.user.dto.UserDto;
import codesquad.team01.issuetracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class IssueService {

	private final IssueRepository issueRepository;
	private final UserRepository userRepository;
	private final LabelRepository labelRepository;

	private final IssueAssembler issueAssembler;
	private final CursorEncoder cursorEncoder;

	public IssueDto.ListResponse findIssues(IssueDto.ListQueryRequest request, CursorDto.CursorData cursor) {

		// 이슈 기본 정보 조회 - (담당자, 레이블 제외)
		List<IssueDto.BaseRow> issues = issueRepository.findIssuesWithFilters(
			request.getIssueState(), request.writerId(), request.milestoneId(),
			request.labelIds(), request.assigneeIds(), cursor);

		boolean hasNext = issues.size() > IssueConstants.PAGE_SIZE; // 다음 페이지 존재 여부

		List<IssueDto.BaseRow> pagedIssues =
			hasNext ? issues.subList(0, IssueConstants.PAGE_SIZE) : issues; // 다음 페이지가 있다면 PAGE_SIZE만큼만

		if (pagedIssues.isEmpty()) {
			log.debug("조건에 맞는 이슈가 없습니다.");
			return IssueDto.ListResponse.builder()
				.issues(List.of())
				.totalCount(0)
				.cursor(CursorDto.CursorResponse.builder()
					.next(null)
					.hasNext(false)
					.build())
				.build();
		}

		// 이슈 ID 목록
		List<Integer> issueIds = pagedIssues.stream()
			.map(IssueDto.BaseRow::issueId)
			.toList();
		log.debug("기본 이슈 {}개 조회, id 목록: {}", pagedIssues.size(), issueIds);

		// 드라이빙 테이블 기준으로 분리
		List<UserDto.IssueAssigneeRow> assignees = userRepository.findAssigneesByIssueIds(issueIds);
		List<LabelDto.IssueLabelRow> labels = labelRepository.findLabelsByIssueIds(issueIds);
		log.debug("이슈 담당자 {}개, 레이블 {}개 조회", assignees.size(), labels.size());

		// 이슈 기본 정보와 담당자, 레이블 조합
		List<IssueDto.Details> issueDetails = issueAssembler.assembleIssueDetails(
			pagedIssues, assignees, labels);

		// 응답 dto로 변환
		List<IssueDto.ListItemResponse> issueResponses = issueDetails.stream()
			.map(IssueDto.Details::toListItemResponse)
			.toList();

		// 커서 생성
		String next = null;
		if (hasNext && !pagedIssues.isEmpty()) { // 다음 페이지 있으면
			IssueDto.BaseRow lastIssue = pagedIssues.getLast();
			CursorDto.CursorData nextCursor = CursorDto.CursorData.builder()
				.id(lastIssue.issueId())
				.createdAt(lastIssue.issueCreatedAt())
				.build();
			log.debug("last issue CreatedAt: {}, issue id={}", lastIssue.issueCreatedAt(), lastIssue.issueId());
			next = cursorEncoder.encode(nextCursor);
		}

		log.debug("응답 데이터 생성 완료: 이슈 {}개 포함, 다음 페이지 존재: {}",
			issueResponses.size(), hasNext);
		return IssueDto.ListResponse.builder()
			.issues(issueResponses)
			.totalCount(issueResponses.size())
			.cursor(CursorDto.CursorResponse.builder()
				.next(next)
				.hasNext(hasNext)
				.build())
			.build();
	}

	public IssueDto.CountResponse countIssues(IssueDto.CountQueryRequest request) {

		log.debug(request.toString());

		IssueDto.CountResponse response =
			issueRepository.countIssuesWithFilters(request.writerId(), request.milestoneId(),
				request.labelIds(), request.assigneeIds());

		log.debug(response.toString());

		return response;
	}
}
