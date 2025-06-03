/** @jsxImportSource @emotion/react */
"use client";

import { useEffect, useState } from "react";
import styled from "@emotion/styled";
import Link from "next/link";
import InfiniteScroll from "react-infinite-scroll-component";

import useSyncFilters from "@/hooks/useSyncFilters";
import { useIssuesInfinite } from "@/hooks/useIssuesInfinite";
import { fetchFilterOptions } from "@/app/api/filters";
import {
  useSelectedFilters,
  useIssueFilterStore,
} from "@/stores/useIssueFilterStore";

import IssueList from "@components/issue/IssueList";
import Button from "@components/issue/Button";
import { FilterGroup, FilterDropdown } from "@components/filter/FilterGroup";

import type { FilterOptions } from "@/types/filter";

const Page = styled.div`
  display: flex;
  min-height: 87.5vh;
  flex-direction: column;
  padding: 1rem 5rem 5rem 5rem;
  background-color: ${({ theme }) => theme.colors.surface.default};
`;

const Toolbar = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
`;

const LeftControls = styled.div`
  display: flex;
  align-items: center;
  width: 38.8vw;
  input {
    height: 2.5rem;
    padding: 0.5rem 1rem;
    font-size: 1rem;
    border: ${({ theme }) =>
      `${theme.border.default} ${theme.colors.border.default}`};
    border-radius: 0 ${({ theme }) => theme.radius.medium}
      ${({ theme }) => theme.radius.medium} 0;
    background-color: ${({ theme }) => theme.colors.surface.bold};
    flex: 1;
  }
`;

const RightControls = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
`;

const IssueFilterDropdown = styled(FilterDropdown)`
  width: 8rem;
  height: 2.5rem;
  border-radius: ${({ theme }) => theme.radius.medium} 0 0
    ${({ theme }) => theme.radius.medium};
`;

const LabelMoveBtn = styled(FilterDropdown)`
  width: 10rem;
  border-radius: ${({ theme }) => theme.radius.medium} 0 0
    ${({ theme }) => theme.radius.medium};
`;

const MileStoneMoveBtn = styled(FilterDropdown)`
  width: 10rem;
  border-radius: 0 ${({ theme }) => theme.radius.medium}
    ${({ theme }) => theme.radius.medium} 0;
`;

export default function IssuesPage() {
  /** 1) URL ↔ Zustand 동기화  */
  useSyncFilters();

  /** 2) 이슈 ‧ 카운트 쿼리  */
  const { issuesQuery, countQuery } = useIssuesInfinite();

  /** 3) 필터 옵션 (레이블/마일스톤/사용자)  */
  /* ① useState 제네릭 명시 */
  const [filterOptions, setFilterOptions] = useState<FilterOptions>({
    label: [],
    milestone: [],
    assignee: [],
    writer: [],
  });

  /* ② async/await 패턴 */
  useEffect(() => {
    const loadOptions = async () => {
      try {
        const data = await fetchFilterOptions();
        setFilterOptions(data);
      } catch (err) {
        console.error("필터 옵션 로딩 실패:", err);
      }
    };
    loadOptions();
  }, []);

  /* 4) 평탄화된 이슈 리스트 */
  const flatIssues =
    issuesQuery.data?.pages.flatMap((p) => p.data.issues) ?? [];

  /* 5) 필터 초기화 버튼 기능 */
  const selected = useSelectedFilters(); // ① 현재 적용된 필터
  const resetFilters = useIssueFilterStore((s) => s.resetFilters);

  const isFiltered = Object.keys(selected).length > 0; // true → 무엇이든 선택됨

  return (
    <Page>
      <Toolbar>
        <LeftControls>
          <IssueFilterDropdown
            label="필터"
            onClick={() => {
              /* 팝업 열기 */
            }}
            hasDownIcon={true}
          />
          <input type="text" placeholder="is:issue is:open" />
        </LeftControls>
        <RightControls>
          <FilterGroup>
            <Link href="/labels">
              <LabelMoveBtn
                label={`레이블(${filterOptions.label.length})`}
                hasDownIcon={false}
                onClick={() => {
                  /* 레이블 팝업 */
                }}
              />
            </Link>
            <Link href="/milestones">
              <MileStoneMoveBtn
                label={`마일스톤(${filterOptions.milestone.length})`}
                hasDownIcon={false}
                onClick={() => {
                  /* 마일스톤 팝업 */
                }}
              />
            </Link>
          </FilterGroup>
          <Link href="/issues/new">
            <Button
              onClick={() => {
                /* 이슈 작성 페이지로 이동 */
              }}
            >
              + 이슈 작성
            </Button>
          </Link>
        </RightControls>
      </Toolbar>

      {isFiltered && (
        <Button css={{ marginLeft: "0.5rem" }} onClick={resetFilters}>
          필터 지우기
        </Button>
      )}

      <InfiniteScroll
        dataLength={flatIssues.length}
        next={issuesQuery.fetchNextPage}
        hasMore={!!issuesQuery.hasNextPage}
        loader={<h4 style={{ textAlign: "center" }}>로딩 중…</h4>}
        endMessage={
          <p style={{ textAlign: "center" }}>
            <b>모든 이슈를 불러왔습니다.</b>
          </p>
        }
      >
        <IssueList
          issues={flatIssues}
          openCount={countQuery.data?.data.open ?? 0}
          closeCount={countQuery.data?.data.closed ?? 0}
        />
      </InfiniteScroll>
    </Page>
  );
}
