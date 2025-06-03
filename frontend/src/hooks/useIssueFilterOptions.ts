// 필터 옵션을 가져오는 훅
import { useState, useEffect } from "react";
import type { FilterOptions } from "@/types/filter";

/** API 한 번 호출해 레이블·마일스톤·유저 옵션을 모두 가져옵니다. */
export default function useIssueFilterOptions() {
  const [options, setOptions] = useState<FilterOptions>({
    assignee: [],
    label: [],
    milestone: [],
    writer: [],
  });

  useEffect(() => {
    (async () => {
      try {
        const [lbl, ms, usr] = await Promise.all([
          fetch("/api/v1/labels/filters").then((r) => r.json()),
          fetch("/api/v1/milestones/filters").then((r) => r.json()),
          fetch("/api/v1/users/filters").then((r) => r.json()),
        ]);

        setOptions({
          label: lbl.data.labels.map((l: any) => ({
            id: String(l.id),
            label: l.name,
            color: l.color,
          })),
          milestone: ms.data.milestones.map((m: any) => ({
            id: String(m.id),
            label: m.title,
          })),
          assignee: usr.data.users.map((u: any) => ({
            id: String(u.id),
            label: u.username,
            iconUrl: u.profileImageUrl,
          })),
          writer: usr.data.users.map((u: any) => ({
            id: String(u.id),
            label: u.username,
            iconUrl: u.profileImageUrl,
          })),
        });
      } catch (err) {
        console.error("필터 옵션 로딩 실패:", err);
      }
    })();
  }, []);

  return options;
}
