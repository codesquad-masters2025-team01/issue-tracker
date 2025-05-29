"use client";

import { useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function OAuthCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  useEffect(() => {
    const code = searchParams.get("code");
    const state = searchParams.get("state");

    if (!code) {
      alert("인증 코드가 없습니다.");
      router.push("/login");
      return;
    }

    const exchangeToken = async () => {
      try {
        const res = await fetch(
          `https://localhost:8080/api/v1/oauth/callback?code=${code}&state=${state}`
          //   `api/v1/oauth/callback?code=${code}&state=${state}`
        );
        const json = await res.json();

        if (json.data?.jwt) {
          localStorage.setItem("token", json.data.jwt); // 또는 쿠키에 저장
          router.push("/issues"); // 로그인 후 이동
        } else {
          alert("토큰 발급에 실패했습니다.");
          router.push("/login");
        }
      } catch (err) {
        console.error("OAuth 처리 오류:", err);
        alert("로그인 처리 중 오류가 발생했습니다.");
        router.push("/login");
      }
    };

    exchangeToken();
  }, [router, searchParams]);

  return <p>로그인 처리 중입니다...</p>;
}
