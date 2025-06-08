"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuthStore } from "@/stores/useAuthStore";

export default function GitHubCallbackPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const setAccessToken = useAuthStore((state) => state.setAccessToken);

useEffect(()=>{
console.log()
},[])


  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");

    if (accessToken) {
      // 전역 상태에 액세스 토큰 저장
      setAccessToken(accessToken);
      // 새로고침 후에도 유지하려면 로컬 스토리지에 저장
      localStorage.setItem("accessToken", accessToken);
    }

    if (refreshToken) {
      localStorage.setItem("refreshToken", refreshToken);
    }

    // 콜백이 처리된 후 리디렉션할 경로
    router.replace("/issues");
  }, [searchParams, setAccessToken, router]);

  return <div>로그인 처리 중...</div>;
}