// src/app/login/page.tsx
/** @jsxImportSource @emotion/react */
"use client";

import Image from "next/image";
import Link from "next/link";
import { useThemeStore } from "@/stores/useThemeStore";
import styled from "@emotion/styled";
import theme from "@/styles/theme";
import ThemeToggleBtn from "@components/theme/ThemeToggleBtn";

const Container = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: ${({ theme }) => theme.colors.surface.default};
`;

const Logo = styled.div`
  color: ${({ theme }) => theme.colors.grayscale[900]};
  margin-top: 1.5rem;
  margin-bottom: 4rem;
`;

const SocialButton = styled.button`
  width: 280px;
  height: 48px;
  border: 1px solid ${({ theme }) => theme.colors.accent.blue};
  border-radius: ${({ theme }) => theme.radius.medium};
  background-color: transparent;
  color: ${({ theme }) => theme.colors.accent.blue};
  font-family: "Pretendard", sans-serif;
  font-weight: 500;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover {
    opacity: ${({ theme }) => theme.opacity.hover};
  }
`;

const Divider = styled.div`
  margin: 16px 0;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.grayscale[600]};
  position: relative;
  span {
    background-color: ${({ theme }) => theme.colors.surface.default};
    padding: 0 8px;
  }
  &::before {
    content: "";
    position: absolute;
    top: 50%;
    left: 0;
    right: 0;
    /* border-top: 1px solid ${({ theme }) => theme.colors.grayscale[400]}; */
    transform: translateY(-50%);
    z-index: 0;
  }
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
`;

const TextInput = styled.input`
  width: 280px;
  height: 48px;
  padding: 0 12px;
  border: none;
  border-radius: ${({ theme }) => theme.radius.large};
  background-color: ${({ theme }) => theme.colors.surface.strong};
  font-size: 16px;
  color: ${({ theme }) => theme.colors.grayscale[900]};
  &::placeholder {
    color: ${({ theme }) => theme.colors.grayscale[500]};
  }
`;

const LoginButton = styled.button`
  width: 280px;
  height: 48px;
  border: none;
  border-radius: ${({ theme }) => theme.radius.medium};
  background-color: ${({ theme }) => theme.colors.accent.blue};
  color: ${({ theme }) => theme.colors.surface.strong};
  font-family: "Pretendard", sans-serif;
  font-weight: 500;
  font-size: 16px;
  cursor: pointer;
  transition: opacity 0.2s;
  &:hover {
    opacity: ${({ theme }) => theme.opacity.hover};
  }
`;

const SignupLink = styled(Link)`
  margin-top: 8px;
  font-size: 14px;
  color: ${({ theme }) => theme.colors.grayscale[600]};
  text-decoration: none;
  &:hover {
    text-decoration: underline;
  }
`;

export default function LoginPage() {
  const isDarkMode = useThemeStore((state) => state.isDark);

  const handleGitHubLogin = () => {
    window.location.href = "http://localhost:8080/api/v1/oauth/github/login";
  };

  return (
    <Container>
      <ThemeToggleBtn />

      <Logo>
        {isDarkMode ? (
          <Image
            src="/icons/logoIconDark.svg"
            alt="이슈 트래커 로고 - 다크모드"
            width={300}
            height={72}
          />
        ) : (
          <Image
            src="/icons/logoIcon.svg"
            alt="이슈 트래커 로고"
            width={300}
            height={72}
          />
        )}
      </Logo>

      <SocialButton onClick={handleGitHubLogin}>
        GitHub 계정으로 로그인
      </SocialButton>

      <Divider>
        <span>or</span>
      </Divider>

      <Form>
        <TextInput type="text" placeholder="아이디" />
        <TextInput type="password" placeholder="비밀번호" />
        <LoginButton>아이디로 로그인</LoginButton>
      </Form>

      <SignupLink href="/signup">회원가입</SignupLink>
    </Container>
  );
}
