"use client";

import IssueListHeaderView from "@components/issue/IssueListHeaderView";
import useIssueFilterOptions from "@/hooks/useIssueFilterOptions";
import {
  useIssueFilterStore,
  useSelectedFilters,
  type FilterKey,
} from "@/stores/useIssueFilterStore";

interface Props {
  openCount: number;
  closeCount: number;
  tab: "open" | "closed";
  onChangeTab: (t: "open" | "closed") => void;
}

export default function IssueListHeaderContainer({
  openCount,
  closeCount,
  tab,
  onChangeTab,
}: Props) {
  /* 1) 옵션·필터 상태 가져오기 */
  const options = useIssueFilterOptions();
  const selected = useSelectedFilters();
  const setFilter = useIssueFilterStore((s) => s.setFilter);
  const clearFilter = useIssueFilterStore((s) => s.clearFilter);

  // 🔹 각 키를 모두 포함
  const normalizedSelected: Record<FilterKey, string | undefined> = {
    assignee: selected.assignee,
    label: selected.label,
    milestone: selected.milestone,
    writer: selected.writer,
  };

  /* 2) 프레젠테이션 컴포넌트에 위임 */
  return (
    <IssueListHeaderView
      options={options}
      selectedFilters={normalizedSelected}
      onSelect={(k: FilterKey, id: string) => setFilter(k, id)}
      onClear={(k: FilterKey) => clearFilter(k)}
      openCount={openCount}
      closeCount={closeCount}
      tab={tab}
      onChangeTab={onChangeTab}
    />
  );
}
