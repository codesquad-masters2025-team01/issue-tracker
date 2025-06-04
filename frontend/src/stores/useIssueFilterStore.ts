import { create } from "zustand";
import { persist } from "zustand/middleware";

/* ─────────────────────────────────── */
/* 1) 타입 정의                         */
export type FilterKey = "assignee" | "label" | "milestone" | "writer";

export interface SelectedFilters {
  assignee?: string;
  label?: string;
  milestone?: string;
  writer?: string;
}

/* 2) 스토어 인터페이스                */
interface FilterStore {
  selected: SelectedFilters;
  setFilter: (key: FilterKey, value: string) => void;
  clearFilter: (key: FilterKey) => void;
  resetFilters: () => void;
}

/* 3) 스토어 생성                      */
export const useIssueFilterStore = create<FilterStore>()(
  persist(
    (set) => ({
      selected: {},

      setFilter: (key, value) =>
        set((s) => ({ selected: { ...s.selected, [key]: value } })),

      clearFilter: (key) =>
        set((s) => {
          const { [key]: _, ...rest } = s.selected;
          return { selected: rest };
        }),

      resetFilters: () => set({ selected: {} }),
    }),
    { name: "issue-filter-storage" } // 로컬스토리지에 유지
  )
);

export const useSelectedFilters = () => useIssueFilterStore((s) => s.selected);
