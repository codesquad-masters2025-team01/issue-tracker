/** @jsxImportSource @emotion/react */
"use client";

import Image from "next/image";
import styled from "@emotion/styled";
import { formatDistanceToNow } from "date-fns";
import { ko } from "date-fns/locale/ko";
import type { Issue } from "@/types/issue";
import type { LabelType } from "@/types/issue";

// 전체 그리드를 checkbox / icon / 내용 / 작성자 Avatar 네 칸으로 분리
const Item = styled.div`
  display: grid;
  /* grid-template-columns: auto 1.5rem 1fr auto; */
  grid-template-columns: auto 1fr auto;
  grid-template-rows: 6rem;
  align-items: center;
  gap: 0.75rem;
  border-top: ${({ theme }) =>
    `${theme.border.default} ${theme.colors.border.default}`};
  background-color: ${({ theme }) => theme.colors.surface.strong};
`;

const IssueCheckbox = styled.input`
  margin: 1.5rem 2rem auto 2.25rem;
  width: 1rem;
  height: 1rem;
`;

const Content = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

// 1행: 제목 + 라벨 배지
const TitleRow = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.25rem;
  font-weight: 500;
  color: ${({ theme }) => theme.colors.neutralText.strong};
`;

// 라벨 배지
const LabelBadge = styled.span`
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.75rem;
  font-size: 0.75rem;
  height: 1.5rem;
  border-radius: ${({ theme }) => theme.radius.large};
  background-color: ${({ theme }) => theme.colors.accent.blue};
  color: white;
`;

// 2행: 메타 정보 (이슈번호 · 시간 · 작성자 · 마일스톤)
const MetaRow = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  font-size: 1rem;
  color: ${({ theme }) => theme.colors.neutralText.weak};
`;

const MilestoneText = styled.span`
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 1rem;
  color: ${({ theme }) => theme.colors.neutralText.weak};
`;

// 작성자 아바타
const Avatar = styled.img`
  width: 1.25rem;
  height: 1.25rem;
  border-radius: 50%;
  margin-right: 3.375rem;
`;

export const IssueItem: React.FC<{ issue: Issue }> = ({ issue }) => {
  const timeAgo = issue
    ? formatDistanceToNow(new Date(issue.createdAt), {
        addSuffix: true,
        locale: ko,
      })
    : "";

  return (
    <Item>
      <IssueCheckbox type="checkbox" />
      {/* 제목 + 메타 */}
      <Content>
        <TitleRow>
          {/* 열림/닫힘 아이콘 */}
          {issue.state === "open" ? (
            <span>
              <Image
                src="/icons/openIssueIcon.svg"
                alt="열린 이슈 아이콘"
                width={16}
                height={16}
              />
            </span>
          ) : (
            <Image
              src="/icons/closedIssueIcon.svg"
              alt="열린 이슈 아이콘"
              width={16}
              height={16}
            />
          )}
          {/* 제목 */}
          {issue.title}
          {/* 라벨 배지 */}
          {issue.labels?.map((label: LabelType) => (
            <LabelBadge key={label.id}>{label.name}</LabelBadge>
          ))}
        </TitleRow>
        <MetaRow>
          <span>#{issue.id}</span>
          <span>
            이 이슈가 {timeAgo}, {issue.writerName}님에 의해 작성되었습니다
          </span>
          {issue.milestone && (
            <MilestoneText>
              <Image
                src="/icons/IconMilestone.svg"
                alt="마일스톤 아이콘"
                width={16}
                height={16}
              />
              {issue.milestone.title}
            </MilestoneText>
          )}
        </MetaRow>
      </Content>
      {/* 작성자 프로필 이미지 */}
      {/* <Avatar
        src={issue.writerProfileImageUrl}
        alt={`${issue.writerName} 프로필`}
      /> */}
      <Avatar src="/icons/profileIcon.svg" alt="이슈 트래커 로고" />
    </Item>
  );
  // return (
  //   <Item>
  //     <input type="checkbox" />

  //     {/* 열림/닫힘 아이콘 */}
  //     <span>🔓</span>

  //     {/* 제목 + 메타 */}
  //     <Content>
  //       <TitleRow>
  //         <div>32 issue</div>
  //         <LabelBadge key={"k label"}>k label</LabelBadge>
  //       </TitleRow>
  //       <MetaRow>
  //         <span>#32</span>
  //         <span>이 이슈가 8분 전,</span>
  //         <span>b님에 의해 작성되었습니다</span>
  //         <MilestoneText>🗓️ 그룹 프로젝트 - h project</MilestoneText>
  //       </MetaRow>
  //     </Content>

  //     {/* 작성자 프로필 이미지 */}
  //     {/* <Avatar
  //       src={issue.writerProfileImageUrl}
  //       alt={`${issue.writerName} 프로필`}
  //     /> */}
  //   </Item>
  // );
};

export default IssueItem;
