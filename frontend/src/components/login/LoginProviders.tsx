// 새로고침 시 토큰을 복원하기 위한 컴포넌트
"use client";
import { useEffect } from "react";
import { useAuthStore } from "@/stores/useAuthStore";

export default function LoginProviders({ children }: { children: React.ReactNode }) {
  const setAccessToken = useAuthStore((s) => s.setAccessToken);

  useEffect(() => {
    const saved = localStorage.getItem("accessToken");
    if (saved) setAccessToken(saved);
  }, [setAccessToken]);

  return <>{children}</>;
}
