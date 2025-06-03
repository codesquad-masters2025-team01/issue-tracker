import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { fetchIssues, fetchIssueCounts } from "@/app/api/issues";
import { useSelectedFilters } from "@/stores/useIssueFilterStore";

export function useIssuesInfinite() {
  const filters = useSelectedFilters();

  const issuesQuery = useInfiniteQuery({
    queryKey: ["issues", filters],

    /** ① v5 필수 옵션  */
    initialPageParam: null as string | null,

    /** ② pageParam(커서) → fetcher로 전달 */
    queryFn: ({ pageParam }) => fetchIssues(filters, pageParam ?? undefined),

    /** ③ 다음 커서 추출 */
    getNextPageParam: (lastPage) =>
      lastPage.data.cursor?.hasNext ? lastPage.data.cursor.next : undefined,
  });

  const countQuery = useQuery({
    queryKey: ["issueCount", filters],
    queryFn: () => fetchIssueCounts(filters),
  });

  return { issuesQuery, countQuery };
}
