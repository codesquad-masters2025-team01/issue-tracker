// src/app/page.tsx
"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/useAuthStore";

export default function HomePage() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const router = useRouter();

  useEffect(() => {
    // ① accessToken 값이 존재 → 로그인 상태
    if (accessToken) {
      router.replace("/issues");
    } else {
      router.replace("/login");
    }
  }, [accessToken, router]);

  // 로딩 스피너를 보여주고 싶다면 여기에서 반환
  return null;
}
