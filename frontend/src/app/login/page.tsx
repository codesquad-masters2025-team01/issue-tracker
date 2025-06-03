// src/app/login/page.tsx
/** @jsxImportSource @emotion/react */
"use client";

import Image from "next/image";
import Link from "next/link";
import { useThemeStore } from "@/stores/useThemeStore";
import styled from "@emotion/styled";
import ThemeToggleBtn from "@components/theme/ThemeToggleBtn";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/useAuthStore";
import { apiFetch } from "@/hooks/useApiLoginFetch";

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

  // Oauth 관련 상태와 함수
  const handleGitHubLogin = () => {
    window.location.href = "http://localhost:8080/api/v1/oauth/github/login";
    // window.location.href = "/api/v1/oauth/github/login";
  };

  // 일반 로그인 관련 상태와 함수
  const router = useRouter();
  const { setAccessToken } = useAuthStore();

  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      /** 백엔드 응답 형태
       * { success:true, data:{ accessToken:"..." } }
       */
      const { data } = await apiFetch<{ data: { accessToken: string } }>(
        "/auth/login",
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ loginId, password }),
        }
      );

      // ① 메모리(전역 상태)에 저장
      setAccessToken(data.accessToken);
      // ② 새로고침 후에도 유지하려면 (선택)
      localStorage.setItem("accessToken", data.accessToken);

      router.replace("/issues"); // 메인으로 이동
    } catch (err: unknown) {
      if (err instanceof Error) {
        alert(err.message);
      } else {
        alert("로그인 실패");
      }
    } finally {
      setLoading(false);
    }
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

      <Form onSubmit={onSubmit}>
        <TextInput
          type="text"
          placeholder="아이디"
          value={loginId}
          onChange={(e) => setLoginId(e.target.value)}
          required
          className="input"
        />
        <TextInput
          type="password"
          placeholder="비밀번호"
          onChange={(e) => setPassword(e.target.value)}
          required
          className="input"
        />
        <LoginButton disabled={loading}>
          {loading ? "로그인 중…" : "아이디로 로그인"}
        </LoginButton>
      </Form>

      <SignupLink href="/signup">회원가입</SignupLink>
    </Container>
  );
}
