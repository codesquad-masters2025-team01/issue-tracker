// URL ↔ Zustand 동기화

import { useRouter, useSearchParams } from "next/navigation";
import { useEffect } from "react";
import {
  useIssueFilterStore,
  useSelectedFilters,
} from "@/stores/useIssueFilterStore";
import { buildIssueQuery } from "@/utils/buildIssueQuery";

/** URL ↔ Store 쌍방향 동기화 */
export default function useSyncFilters() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setFilter = useIssueFilterStore((s) => s.setFilter);
  const selected = useSelectedFilters();

  /* 1. 최초 마운트 시 URL → Store */
  useEffect(() => {
    const map = {
      label: searchParams.get("labels"),
      milestone: searchParams.get("milestone"),
      assignee: searchParams.get("assignees"),
      writer: searchParams.get("author"),
    } as const;
    for (const key of Object.keys(map) as Array<keyof typeof map>) {
      if (map[key] !== null) {
        setFilter(key, map[key]);
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /* 2. Store → URL */
  useEffect(() => {
    const qs = buildIssueQuery(selected);
    router.replace(qs ? `/issues?${qs}` : "/issues", { scroll: false });
  }, [selected, router]);
}
