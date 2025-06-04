package codesquad.team01.issuetracker.label.repository.impl;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import codesquad.team01.issuetracker.label.domain.LabelTextColor;
import codesquad.team01.issuetracker.label.dto.LabelDto;
import codesquad.team01.issuetracker.label.repository.LabelQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Repository
public class LabelQueryRepositoryImpl implements LabelQueryRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FIND_VALID_LABEL_IDS_QUERY = """
		SELECT id FROM label 
		WHERE id IN (:labelIds) 
		AND deleted_at IS NULL
		""";

	private static final String LABEL_QUERY = """
		    SELECT 
		        il.issue_id,
		        l.id as label_id,
		        l.name as label_name,
		        l.color as label_color,
		        l.text_color as label_text_color
		    FROM issue_label il
		    JOIN label l ON il.label_id = l.id
		    WHERE il.issue_id IN (:issueIds)
			AND l.deleted_at IS NULL
		""";

	private static final String SINGLE_ISSUE_LABEL_QUERY = """
		SELECT 
			l.id as label_id,
			l.name as label_name,
			l.color as label_color,
			l.text_color as label_text_color
		FROM issue_label il
		JOIN label l ON il.label_id = l.id
		WHERE il.issue_id = :issueId
		AND l.deleted_at IS NULL
		ORDER BY l.name ASC
		""";

	private static final String FILTER_LABEL_LIST_QUERY = """
		SELECT 
			l.id as label_id,
			l.name as label_name,
			l.color as label_color
		FROM label l
		WHERE l.deleted_at IS NULL
		ORDER BY l.name ASC
		""";

	private final RowMapper<LabelDto.IssueLabelRow> labelRowMapper = (rs, rowNum) -> LabelDto.IssueLabelRow.builder()
		.issueId(rs.getInt("issue_id"))
		.labelId(rs.getInt("label_id"))
		.labelName(rs.getString("label_name"))
		.labelColor(rs.getString("label_color"))
		.labelTextColor(LabelTextColor.fromTextColorStr(rs.getString("label_text_color")))
		.build();

	private final RowMapper<LabelDto.IssueDetailLabelRow> labelDetailRowMapper =
		(rs, rowNum) -> LabelDto.IssueDetailLabelRow.builder()
			.id(rs.getInt("label_id"))
			.name(rs.getString("label_name"))
			.color(rs.getString("label_color"))
			.textColor(rs.getString("label_text_color"))
			.build();

	private final RowMapper<LabelDto.LabelFilterResponse> filterLabelListItemResponseRowMapper =
		(rs, rowNum) -> LabelDto.LabelFilterResponse.builder()
			.id(rs.getInt("label_id"))
			.name(rs.getString("label_name"))
			.color(rs.getString("label_color"))
			.build();

	@Override
	public List<LabelDto.IssueLabelRow> findLabelsByIssueIds(List<Integer> issueIds) {

		if (issueIds.isEmpty()) {
			return List.of();
		}
		MapSqlParameterSource params = new MapSqlParameterSource("issueIds", issueIds);
		return jdbcTemplate.query(LABEL_QUERY, params, labelRowMapper);
	}

	@Override
	public List<LabelDto.LabelFilterResponse> findLabelsForFilter() {
		return jdbcTemplate.query(FILTER_LABEL_LIST_QUERY, filterLabelListItemResponseRowMapper);
	}

	@Override
	public List<LabelDto.IssueDetailLabelRow> findLabelsByIssueId(Integer issueId) {
		MapSqlParameterSource params = new MapSqlParameterSource("issueId", issueId);
		return jdbcTemplate.query(SINGLE_ISSUE_LABEL_QUERY, params, labelDetailRowMapper);
	}

	@Override
	public List<Integer> findValidLabelIds(List<Integer> labelIds) {
		if (labelIds.isEmpty()) {
			return List.of();
		}

		MapSqlParameterSource params = new MapSqlParameterSource("labelIds", labelIds);

		try {
			return jdbcTemplate.queryForList(FIND_VALID_LABEL_IDS_QUERY, params, Integer.class);
		} catch (DataAccessException e) {
			log.warn("유효한 레이블 ID 조회 중 오류 발생: {}", e.getMessage());
			return List.of();
		}
	}
}
