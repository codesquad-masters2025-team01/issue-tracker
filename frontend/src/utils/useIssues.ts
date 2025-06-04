import { useQuery } from "@tanstack/react-query";
import { useIssueFilterStore } from "@/stores/useIssueFilterStore";
import { buildIssueQuery } from "@/utils/buildIssueQuery";
import type { Issue } from "@/types/issue";

interface UseIssuesOption {
  status: "open" | "closed";
}

export function useIssues({ status }: UseIssuesOption) {
  /* 1) 전역 필터 상태 구독 (shallow 비교로 불필요 렌더 최소화) */
  const selected = useIssueFilterStore((s) => s.selected);

  /* 2) React Query: key 에 필터값 포함 → 값이 바뀌면 자동 refetch */
  const query = buildIssueQuery(selected);

  /* 3) React Query 호출 */
  return useQuery<Issue[]>({
    queryKey: ["issues", status, query], // ← 캐싱 key
    queryFn: async () => {
      const url = `/api/v1/issues?${query}&status=${status}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error("이슈 목록 로드 실패");
      const data = await res.json();
      return data.data as Issue[];
    },
  });
}
