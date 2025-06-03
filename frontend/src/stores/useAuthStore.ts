import { create } from "zustand";

interface AuthState {
  accessToken?: string;
  setAccessToken: (t?: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: undefined,
  setAccessToken: (t) => set({ accessToken: t }),
  logout: () => {
    set({ accessToken: undefined });
    localStorage.removeItem("accessToken");
    // refresh 쿠키 삭제: 서버에 POST /auth/logout 호출 권장
  },
}));
