// src/libs/apiFetch.ts
import { useAuthStore } from "@/stores/useAuthStore";

const BASE_URL = "https://localhost:8080/api/v1";
// const BASE_URL = "/api/v1";

export async function apiFetch<T>(
  input: RequestInfo,
  init: RequestInit = {}
): Promise<T> {
  const { accessToken, setAccessToken, logout } = useAuthStore.getState();

  // 1) 헤더에 Access Token 삽입
  const headers = new Headers(init.headers);
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const makeRequest = () =>
    fetch(typeof input === "string" ? BASE_URL + input : input, {
      ...init,
      credentials: "include", // refresh 쿠키를 위해 **필수**
      headers,
    });

  let res = await makeRequest();

  // 2) 만료 → refresh 시도
  if (res.status === 401) {
    const refreshRes = await fetch(`${BASE_URL}/auth/refresh`, {
      method: "POST",
      credentials: "include",
    });

    if (refreshRes.ok) {
      const { data } = await refreshRes.json(); // { accessToken: "..." }
      setAccessToken(data.accessToken);

      headers.set("Authorization", `Bearer ${data.accessToken}`);
      res = await makeRequest(); // 원요청 재시도
    } else {
      logout(); // refresh 실패 → 로그아웃
      throw new Error("세션이 만료되었습니다. 다시 로그인하세요.");
    }
  }

  if (!res.ok) throw new Error(await res.text());
  return res.json() as Promise<T>;
}
