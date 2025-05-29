import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import lightTheme, { darkTheme } from "@/styles/theme";

// 스토어의 상태 타입
interface ThemeState {
  isDark: boolean;
  current: typeof lightTheme;
  toggle: () => void;
}

// persist 옵션을 쓰면 새로고침해도 유지됩니다.
export const useThemeStore = create<ThemeState>()(
  persist(
    (set, get) => ({
      isDark: false,
      current: lightTheme,
      toggle: () => {
        const next = !get().isDark;
        set({
          isDark: next,
          current: next ? darkTheme : lightTheme,
        });
      },
    }),
    {
      name: "app-theme", // localStorage 키
      storage: createJSONStorage(() => localStorage),
      // partialize: (state) => ({ isDark: state.isDark }), // 저장할 값만 골라서 저장할 수도 있음
    }
  )
);
