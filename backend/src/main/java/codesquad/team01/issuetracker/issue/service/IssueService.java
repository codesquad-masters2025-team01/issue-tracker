package codesquad.team01.issuetracker.issue.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.team01.issuetracker.common.dto.CursorDto;
import codesquad.team01.issuetracker.common.exception.InvalidParameterException;
import codesquad.team01.issuetracker.common.util.CursorEncoder;
import codesquad.team01.issuetracker.issue.constants.IssueConstants;
import codesquad.team01.issuetracker.issue.domain.IssueState;
import codesquad.team01.issuetracker.issue.dto.IssueDto;
import codesquad.team01.issuetracker.issue.exception.ClosedIssueModificationException;
import codesquad.team01.issuetracker.issue.exception.IssueAccessForbiddenException;
import codesquad.team01.issuetracker.issue.repository.IssueRepository;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.label.repository.LabelRepository;
import codesquad.team01.issuetracker.milestone.exception.MilestoneNotFoundException;
import codesquad.team01.issuetracker.milestone.repository.MilestoneRepository;
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
	private final MilestoneRepository milestoneRepository;

	private final IssueUpdateCommandFactory issueUpdateCommandFactory;
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

	@Transactional
	public IssueDto.BatchUpdateResponse batchUpdateIssueState(IssueDto.BatchUpdateRequest request) {
		List<Integer> issueIds = request.issueIds();
		IssueState targetState = request.getTargetState();
		LocalDateTime now = LocalDateTime.now();

		log.debug("{}개 이슈 {} 상태로 변화 시작", issueIds.size(), targetState);

		// 요청으로 들어온 issueIds 중 실제로 존재하는 issue들만 조회
		List<IssueDto.BatchIssueRow> existingIssues = issueRepository.findExistingIssuesByIds(issueIds);

		// 조회한 issue들의 id 추출
		Set<Integer> foundIssueIds = existingIssues.stream()
			.map(IssueDto.BatchIssueRow::issueId)
			.collect(Collectors.toSet());

		// issueIds 중 조회하지 못한 ids - 존재하지 않거나 삭제된 이슈 ids
		List<Integer> failedIssueIds = issueIds.stream()
			.filter(id -> !foundIssueIds.contains(id))
			.toList();

		// 업데이트 해야하는 이슈 ids
		List<Integer> issuesToUpdate = existingIssues.stream()
			.filter(issue -> issue.currentState() != targetState)
			.map(IssueDto.BatchIssueRow::issueId)
			.toList();

		// 이미 targetState인 이슈 개수
		int alreadyInTargetStateCount = existingIssues.size() - issuesToUpdate.size();

		// 실제 업데이트된 이슈 개수
		int updatedCount = 0;

		// 배치 업데이트
		if (!issuesToUpdate.isEmpty()) {
			updatedCount = issueRepository.batchUpdateIssueStates(issuesToUpdate, targetState, now);

			if (updatedCount != issuesToUpdate.size()) {
				log.warn("예상 업데이트 수와 실제 업데이트된 이슈 개수가 다릅니다. 예상: {}, 실제: {}",
					issuesToUpdate.size(), updatedCount);
			}
		}

		int successCount = updatedCount + alreadyInTargetStateCount;
		int failedCount = failedIssueIds.size();

		log.info("배치 상태 업데이트 종료 - 전체: {}, 성공: {}, 실패: {}, 이미 해당 상태: {}",
			issueIds.size(), successCount, failedCount, alreadyInTargetStateCount);

		return IssueDto.BatchUpdateResponse.builder()
			.totalCount(issueIds.size())
			.successCount(successCount)
			.failedCount(failedCount)
			.failedIssueIds(failedIssueIds)
			.build();
	}

	@Transactional
	public IssueDto.CreateResponse createIssue(IssueDto.CreateRequest request, Integer firstUserId) {
		LocalDateTime now = LocalDateTime.now();

		log.info("사용자 {}가 이슈 생성 요청", firstUserId);

		if (request.milestoneId() != null) {
			validateMilestoneExists(request.milestoneId());
		}

		List<Integer> validLabelIds = labelRepository.findValidLabelIds(request.labelIds());
		if (validLabelIds.size() != request.labelIds().size()) {
			List<Integer> invalidLabelIds = request.labelIds().stream()
				.filter(li -> !validLabelIds.contains(li))
				.toList();
			log.warn("유효하지 않은 레이블 ID 제외: {}", invalidLabelIds);
		}

		List<Integer> validAssigneeIds = userRepository.findValidUserIds(request.assigneeIds());
		if (validAssigneeIds.size() != request.assigneeIds().size()) {
			List<Integer> invalidAssigneeIds = request.assigneeIds().stream()
				.filter(ai -> !validAssigneeIds.contains(ai))
				.toList();
			log.warn("유효하지 않은 담당자 ID 제외: {}", invalidAssigneeIds);
		}

		Integer issueId = issueRepository.createIssue(
			request.title(),
			request.content(),
			firstUserId,
			request.milestoneId(),
			now
		);

		if (!validLabelIds.isEmpty()) {
			issueRepository.addLabelsToIssue(issueId, validLabelIds);
		}

		if (!validAssigneeIds.isEmpty()) {
			issueRepository.addAssigneesToIssue(issueId, validAssigneeIds);
		}

		IssueDto.DetailBaseRow detailBaseRow = issueRepository.findCreatedIssueById(issueId);
		List<LabelDto.IssueDetailLabelRow> labelRows = labelRepository.findLabelsByIssueId(issueId);
		List<UserDto.IssueDetailAssigneeRow> assigneeRows = userRepository.findAssigneesByIssueId(issueId);

		return issueAssembler.assembleSingleIssueDetails(detailBaseRow, labelRows, assigneeRows);
	}

	@Transactional
	public IssueDto.CreateResponse updateIssue(Integer issueId, IssueDto.UpdateRequest request, Integer userId) {

		// 단일 필드 수정 검증
		if (!request.hasExactlyOneField()) {
			throw new InvalidParameterException("한 번에 하나의 필드만 수정할 수 있습니다.");
		}

		// 이슈 존재 확인 + 해당 issue의 state, writerId 가져오기
		IssueDto.IssueStateAndWriterIdRow stateAndWriterIdRow = issueRepository.findIssueStateAndWriterIdByIssueId(
			issueId);

		// 작성자가 로그인된 사용자인지 확인
		if (stateAndWriterIdRow.writerId() != userId) {
			throw new IssueAccessForbiddenException(issueId, userId);
		}

		// 기존 상태가 CLOSED인 경우 제목, 내용 수정 불가
		if (stateAndWriterIdRow.state() == IssueState.CLOSED) {
			if (request.isUpdatingTitle()) {
				throw new ClosedIssueModificationException("제목");
			}
			if (request.isUpdatingContent()) {
				throw new ClosedIssueModificationException("내용");
			}
		}

		LocalDateTime now = LocalDateTime.now();
		IssueUpdateCommand command = issueUpdateCommandFactory.getCommand(request);
		command.execute(issueUpdateCommandFactory, issueId, request, now);

		IssueDto.DetailBaseRow detailBaseRow = issueRepository.findCreatedIssueById(issueId);
		List<LabelDto.IssueDetailLabelRow> labelRows = labelRepository.findLabelsByIssueId(issueId);
		List<UserDto.IssueDetailAssigneeRow> assigneeRows = userRepository.findAssigneesByIssueId(issueId);
		// int commentCount = commentRepository.findCommentCountByIssueId(issueId); // 댓글 구현 시

		return issueAssembler.assembleSingleIssueDetails(detailBaseRow, labelRows,
			assigneeRows, 0);// todo: 댓글 처리 후 0을 commentCount로 변경 필요
	}

	private void validateMilestoneExists(Integer milestoneId) {
		if (milestoneId == null) { // 마일스톤이 없는 이슈
			return;
		}

		if (!milestoneRepository.existsMilestone(milestoneId)) {
			throw new MilestoneNotFoundException("존재하지 않는 마일스톤: " + milestoneId);
		}
	}

}
