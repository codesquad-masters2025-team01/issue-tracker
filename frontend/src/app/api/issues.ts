// 이슈 메인 페이지 - fetcher 모음
import type { SelectedFilters } from "@/stores/useIssueFilterStore";
import { buildIssueQuery } from "@/utils/buildIssueQuery";

export async function fetchIssues(filters: SelectedFilters, cursor?: string) {
  const qsParts = [buildIssueQuery(filters), cursor && `cursor=${cursor}`];
  const qs = qsParts.filter(Boolean).join("&");
  const res = await fetch(`/api/v1/issues${qs ? `?${qs}` : ""}`);
  if (!res.ok) throw new Error("이슈 목록 API 실패");
  return res.json(); // { data: { issues, cursor } }
}

export async function fetchIssueCounts(filters: SelectedFilters) {
  const qs = buildIssueQuery(filters);
  const res = await fetch(`/api/v1/issues/count${qs ? `?${qs}` : ""}`);
  if (!res.ok) throw new Error("이슈 카운트 API 실패");
  return res.json(); // { data: { open, closed } }
}
