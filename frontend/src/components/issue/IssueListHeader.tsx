/** @jsxImportSource @emotion/react */
"use client";

import { useState, useEffect, useMemo } from "react";
import { useIssueFilterStore } from "@/stores/useIssueFilterStore";
import styled from "@emotion/styled";
import IssueOpenTabFilter from "@components/filter/IssueOpenTabFilter";
import FilterDropdownButton from "@components/filter/FilterDropdownButton";

type FilterKey = "assignee" | "label" | "milestone" | "writer";

interface Option {
  id: string;
  label: string;
  color?: string;
}

interface ListHeaderProps {
  openCount: number;
  closeCount: number;
  selected: "open" | "closed";
  onChangeTab: (status: "open" | "closed") => void;
}

/* ----------------- 스타일 ----------------- */
const HeaderWrapper = styled.div`
  display: flex;
  align-items: center;
  padding: 16px 32px;
  background-color: ${({ theme }) => theme.colors.surface.default};
  color: ${({ theme }) => theme.colors.neutralText.default};
`;

const LeftSection = styled.div`
  display: flex;
  align-items: center;
  flex: 1;
  gap: 2rem;
`;

const RightSection = styled.div`
  display: flex;
  gap: 1rem;
`;

const Checkbox = styled.input`
  width: 1rem;
  height: 1rem;
  cursor: pointer;
`;

/* ----------------- 데이터 훅 ----------------- */
function useIssueFilterOptions() {
  const [options, setOptions] = useState<{
    assignee: Option[];
    label: Option[];
    milestone: Option[];
    writer: Option[];
  }>({ assignee: [], label: [], milestone: [], writer: [] });

  useEffect(() => {
    (async () => {
      const [lbl, ms, usr] = await Promise.all([
        // fetch("/api/v1/issue-metadata/labels").then((r) => r.json()),
        // fetch("/api/v1/issue-metadata/milestones").then((r) => r.json()),
        // fetch("/api/v1/issue-metadata/users").then((r) => r.json()),
        fetch("/mockDatas/labelFilterMock.json").then((r) => r.json()),
        fetch("/mockDatas/milestoneFilterMock.json").then((r) => r.json()),
        fetch("/mockDatas/userFilterMock.json").then((r) => r.json()),
      ]);

      setOptions({
        label: lbl.data.labels.map(
          (l: { id: number; name: string; color: string }) => ({
            id: String(l.id),
            label: l.name,
            color: l.color,
          })
        ),
        milestone: (ms.data.milestones as { id: number; title: string }[]).map(
          (m) => ({
            id: String(m.id),
            label: m.title,
          })
        ),
        assignee: usr.data.users.map((u: { id: number; username: string }) => ({
          id: String(u.id),
          label: u.username,
        })),
        writer: usr.data.users.map((u: { id: number; username: string }) => ({
          id: String(u.id),
          label: u.username,
        })), // assignee와 동일하게 처리
      });
    })();
  }, []);

  return options;
}

/* ----------------- 본 컴포넌트 ----------------- */
export default function IssueListHeader({
  openCount,
  closeCount,
  selected,
  onChangeTab,
}: ListHeaderProps) {
  const options = useIssueFilterOptions();

  /** 제목·옵션 매핑 */
  const titleMap: Record<FilterKey, string> = {
    assignee: "담당자",
    label: "레이블",
    milestone: "마일스톤",
    writer: "작성자",
  };

  const rightButtons = useMemo<FilterKey[]>(
    () => ["assignee", "label", "milestone", "writer"],
    []
  );

  return (
    <HeaderWrapper>
      {/* ---------- 좌측: 체크박스 + 오픈/닫힘 탭 ---------- */}
      <LeftSection>
        <Checkbox type="checkbox" />
        <IssueOpenTabFilter
          openCount={openCount}
          closeCount={closeCount}
          selected={selected}
          onChangeTab={onChangeTab}
        />
      </LeftSection>

      {/* ---------- 우측: 필터 드롭다운 버튼들 ---------- */}
      <RightSection>
        {rightButtons.map((key) => (
          <FilterDropdownButton
            key={key}
            filterKey={key}
            title={titleMap[key]}
            options={options[key]}
            /* ▼ ① 전역 selected 값 읽기 */
            selectedId={useIssueFilterStore.getState().selected[key]}
            /* ▼ ② 전역 setFilter 로 저장 */
            onSelect={(id) => useIssueFilterStore.getState().setFilter(key, id)}
          />
        ))}
      </RightSection>
    </HeaderWrapper>
  );
}
