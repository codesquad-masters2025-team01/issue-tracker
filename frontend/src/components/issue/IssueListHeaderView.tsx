/** @jsxImportSource @emotion/react */
"use client";

import styled from "@emotion/styled";
import IssueOpenTabFilter from "@components/filter/IssueOpenTabFilter";
import FilterDropdownButton from "@components/filter/FilterDropdownButton";
import type { FilterKey } from "@/stores/useIssueFilterStore";
import type { FilterOptions } from "@/types/filter";

/* 스타일 ---------------------- */
const HeaderWrapper = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 32px;
  background: ${({ theme }) => theme.colors.surface.default};
  color: ${({ theme }) => theme.colors.neutralText.default};
`;
const Left = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
  gap: 2rem;
`;
const Right = styled.div`
  display: flex;
  gap: 1rem;
`;
const Checkbox = styled.input`
  width: 1rem;
  height: 1rem;
  cursor: pointer;
`;

/* 프롭 타입 ------------------- */
interface Props {
  options: FilterOptions;
  selectedFilters: Record<FilterKey, string | undefined>;
  onSelect: (key: FilterKey, id: string) => void;
  onClear: (key: FilterKey) => void;
  openCount: number;
  closeCount: number;
  tab: "open" | "closed";
  onChangeTab: (t: "open" | "closed") => void;
}

const buttonKeys: FilterKey[] = ["assignee", "label", "milestone", "writer"];
const title: Record<FilterKey, string> = {
  assignee: "담당자",
  label: "레이블",
  milestone: "마일스톤",
  writer: "작성자",
};

export default function IssueListHeaderView({
  options,
  selectedFilters,
  onSelect,
  onClear,
  openCount,
  closeCount,
  tab,
  onChangeTab,
}: Props) {
  return (
    <HeaderWrapper>
      <Left>
        <Checkbox type="checkbox" />
        <IssueOpenTabFilter
          openCount={openCount}
          closeCount={closeCount}
          selected={tab}
          onChangeTab={onChangeTab}
        />
      </Left>

      <Right>
        {buttonKeys.map((k) => (
          <FilterDropdownButton
            key={k}
            filterKey={k}
            title={title[k]}
            options={options[k]}
            selectedId={selectedFilters[k]}
            onSelect={(id) => (id ? onSelect(k, id) : onClear(k))}
          />
        ))}
      </Right>
    </HeaderWrapper>
  );
}
