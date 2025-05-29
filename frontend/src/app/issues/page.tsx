/** @jsxImportSource @emotion/react */
"use client";

import { useEffect, useState } from "react";
import styled from "@emotion/styled";
import Button from "@components/issue/Button";
import { FilterGroup, FilterDropdown } from "@components/filter/FilterGroup";
import IssueListComponent from "@components/issue/IssueList";
import type { Issue } from "@/types/issue";
import Link from "next/link";
import InfiniteScroll from "react-infinite-scroll-component";

const Page = styled.div`
  display: flex;
  height: 105vh;
  flex-direction: column;
  padding: 1rem 5rem 5rem 5rem;
  background-color: ${({ theme }) => theme.colors.surface.default};
`;

const Toolbar = styled.div`
  display: flex;
  height: 105vh;
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
  const [issues, setIssues] = useState<Issue[]>([]);

  // 무한 스크롤에 필요한 상태 관리
  const [cursor, setCursor] = useState<string | null>(null);
  const [hasNext, setHasNext] = useState<boolean>(true);
  const [loading, setLoading] = useState<boolean>(false);

  // 열린 이슈, 닫힌 이슈 개수(이슈 카운트) 상태 관리
  const [issueCounts, setIssueCounts] = useState({ open: 0, closed: 0 });

  // 이슈 카운트 API 호출
  const fetchIssueCounts = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/v1/issues/count");
      if (!res.ok) throw new Error("이슈 카운트 API 호출 실패");
      const json = await res.json();
      setIssueCounts(json.data);
    } catch (err) {
      console.error(err);
    }
  };

  // 필터에 대한 상태 관리
  // const [popupType, setPopupType] = useState<
  //   null | "assignee" | "label" | "milestone" | "writer"
  // >(null);

  // // 필터 옵션 상태
  // const [filterOptions, setFilterOptions] = useState({
  //   assignee: [],
  //   label: [],
  //   milestone: [],
  //   writer: [],
  // });

  // // 선택된 필터 상태
  // const [selectedFilters, setSelectedFilters] = useState({
  //   assignee: undefined,
  //   label: undefined,
  //   milestone: undefined,
  //   writer: undefined,
  // });

  // 필터 데이터 비동기 요청
  // const fetchFilterOptions = async () => {
  //   try {
  //     const [labelRes, milestoneRes, userRes] = await Promise.all([
  //       fetch("/api/v1/issue-metadata/labels"),
  //       fetch("/api/v1/issue-metadata/milestones"),
  //       fetch("/api/v1/issue-metadata/users"),
  //     ]);

  //     const [labelJson, milestoneJson, userJson] = await Promise.all([
  //       labelRes.json(),
  //       milestoneRes.json(),
  //       userRes.json(),
  //     ]);

  //     const authors: { id: string; label: string }[] = [...new Set(issues.map((i) => i.writerName))].map(
  //       (name, idx) => ({
  //         id: String(idx + 1),
  //         label: name,
  //       })
  //     );

  //     setFilterOptions({
  //       label: labelJson.data.labels.map((l) => ({
  //         id: String(l.id),
  //         label: l.name,
  //         color: l.color,
  //       })),
  //       milestone: milestoneJson.data.milestones.map((m) => ({
  //         id: String(m.id),
  //         label: m.title,
  //       })),
  //       assignee: userJson.data.users.map((u) => ({
  //         id: String(u.id),
  //         label: u.username,
  //         iconUrl: u.profileImageUrl,
  //       })),
  //       writer: authors,
  //     });
  //   } catch (err) {
  //     console.error(err);
  //   }
  // };

  // fetch("/api/v1/issues?state=open")
  useEffect(() => {
    fetchIssueCounts();
  }, []);

  useEffect(() => {
    fetchIssues();
    // fetchMoreIssues();
  }, []);

  // mock 데이터를 사용하여 이슈 목록을 가져오는 함수 (삭제 예정)
  const fetchIssues = async () => {
    try {
      const res = await fetch("/mockDatas/issueMockData.json");
      if (!res.ok) {
        throw new Error(`HTTP error! status: ${res.status}`);
      }
      const json = await res.json();
      const data = json.data.issues;
      setIssues(data);
    } catch (error) {
      console.error(error);
    }
  };

  // 무한 스크롤을 위한 이슈 목록을 가져오는 함수
  const fetchMoreIssues = async () => {
    if (loading || !hasNext) return;
    setLoading(true);
    try {
      const url = cursor
        ? `http://localhost:8080/api/v1/issues?cursor=${cursor}`
        : "http://localhost:8080/api/v1/issues";
      const res = await fetch(url);
      const result = await res.json();
      const {
        issues: newIssues,
        cursor: { next, hasNext: nextExists },
      } = result.data;

      setIssues((prev) => [...prev, ...newIssues]);
      setCursor(next);
      setHasNext(nextExists);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

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
                label={`레이블(${3})`}
                hasDownIcon={false}
                onClick={() => {
                  /* 레이블 팝업 */
                }}
              />
            </Link>
            <Link href="/milestones">
              <MileStoneMoveBtn
                label={`마일스톤(${2})`}
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

      <InfiniteScroll
        className="your-scroll-container" /* 스크롤 바 없애기 */
        dataLength={issues.length}
        next={fetchMoreIssues}
        hasMore={hasNext}
        loader={<h4 style={{ textAlign: "center" }}>로딩 중...</h4>}
        endMessage={
          <p style={{ textAlign: "center" }}>
            <b>모든 이슈를 불러왔습니다.</b>
          </p>
        }
      >
        <IssueListComponent
          issues={issues}
          openCount={issueCounts.open}
          closeCount={issueCounts.closed}
        />
      </InfiniteScroll>
    </Page>
  );
}
