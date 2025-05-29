/** @jsxImportSource @emotion/react */
"use client";

import styled from "@emotion/styled";
import { useTheme } from "@emotion/react";
// import { PaperclipIcon, UserIcon } from "@/components/icons";

const Container = styled.div`
  /* position: absolute; */
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  padding: 2rem;
  background-color: ${({ theme }) => theme.colors.surface.default};
`;

const TitleWrapper = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100vw;
  padding: 0rem 5rem;
`;

const Title = styled.h1`
  font-size: 2rem;
  font-weight: 700;
  color: ${({ theme }) => theme.colors.neutralText.strong};
  margin-bottom: 2rem;
`;

const ContentWrapper = styled.div`
  display: flex;
  flex-direction: row;
  gap: 2rem;
  padding: 0rem 3rem;
  width: 100%;
`;

const InputSection = styled.section`
  flex: 3;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const TitleInput = styled.div`
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background-color: ${({ theme }) => theme.colors.surface.bold};
  padding: 1rem;
  border-radius: ${({ theme }) => theme.radius.large};
  border: ${({ theme }) => theme.border.default};
  border-color: ${({ theme }) => theme.colors.border.default};
`;

const TitleField = styled.input`
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: ${({ theme }) => theme.colors.neutralText.default};
  font-size: 16px;
`;

const CommentField = styled.textarea`
  min-height: 200px;
  padding: 1rem;
  border-radius: ${({ theme }) => theme.radius.large};
  border: ${({ theme }) => theme.border.default};
  border-color: ${({ theme }) => theme.colors.border.default};
  background-color: ${({ theme }) => theme.colors.surface.strong};
  color: ${({ theme }) => theme.colors.neutralText.default};
  resize: vertical;
`;

const FileAttach = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  color: ${({ theme }) => theme.colors.neutralText.weak};
  font-size: 14px;
`;

const AttachText = styled.span`
  text-decoration: underline;
`;

const Sidebar = styled.aside`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background-color: ${({ theme }) => theme.colors.surface.strong};
  padding: 1rem;
  border-radius: ${({ theme }) => theme.radius.large};
`;

const SidebarItem = styled.div`
  padding: 0.75rem 1rem;
  background-color: ${({ theme }) => theme.colors.surface.default};
  border-radius: ${({ theme }) => theme.radius.medium};
  color: ${({ theme }) => theme.colors.neutralText.default};
`;

const Footer = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  max-width: 800px;
  width: 100%;
  margin-top: 2rem;
`;

const CancelButton = styled.button`
  background: transparent;
  color: ${({ theme }) => theme.colors.neutralText.default};
  padding: 0.75rem 1.25rem;
  border-radius: ${({ theme }) => theme.radius.medium};
  border: ${({ theme }) => theme.border.default};
  border-color: ${({ theme }) => theme.colors.border.default};
  cursor: pointer;
`;

const SubmitButton = styled.button`
  background-color: ${({ theme }) => theme.colors.brandSurface.default};
  color: ${({ theme }) => theme.colors.brandText.default};
  padding: 0.75rem 1.25rem;
  border-radius: ${({ theme }) => theme.radius.medium};
  border: none;
  cursor: pointer;

  &:hover {
    opacity: ${({ theme }) => theme.opacity.hover};
  }
`;

export default function NewIssuePage() {
  const theme = useTheme();

  return (
    <Container>
      <TitleWrapper>
        <Title>새로운 이슈 작성</Title>
      </TitleWrapper>
      <ContentWrapper>
        <InputSection>
          <TitleInput>
            {/* <UserIcon /> */}
            <TitleField placeholder="제목" />
          </TitleInput>

          <CommentField placeholder="코멘트를 입력하세요" />

          <FileAttach>
            {/* <PaperclipIcon /> */}
            <AttachText>파일 첨부하기</AttachText>
          </FileAttach>
        </InputSection>

        <Sidebar>
          <SidebarItem>담당자</SidebarItem>
          <SidebarItem>레이블</SidebarItem>
          <SidebarItem>마일스톤</SidebarItem>
        </Sidebar>
      </ContentWrapper>

      <Footer>
        <CancelButton>작성 취소</CancelButton>
        <SubmitButton>완료</SubmitButton>
      </Footer>
    </Container>
  );
}
