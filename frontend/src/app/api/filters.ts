// 이슈 메인 페이지 - 필터 옵션 fetcher
import type { FilterOptions } from "@/types/filter";

export async function fetchFilterOptions(): Promise<FilterOptions> {
  const [labelRes, milestoneRes, userRes] = await Promise.all([
    fetch("/api/v1/labels/filters"),
    fetch("/api/v1/milestones/filters"),
    fetch("/api/v1/users/filters"),
  ]);
  const [labelJson, milestoneJson, userJson] = await Promise.all([
    labelRes.json(),
    milestoneRes.json(),
    userRes.json(),
  ]);

  interface Label {
    id: number;
    name: string;
    color: string;
  }

  interface Milestone {
    id: number;
    title: string;
  }

  interface User {
    id: number;
    username: string;
    profileImageUrl: string;
  }

  interface LabelOption {
    id: string;
    label: string;
    color: string;
  }

  interface MilestoneOption {
    id: string;
    label: string;
  }

  interface UserOption {
    id: string;
    label: string;
    iconUrl: string;
  }

  interface FetchFilterOptionsResult {
    label: LabelOption[];
    milestone: MilestoneOption[];
    assignee: UserOption[];
    writer: UserOption[];
  }

  return {
    label: labelJson.data.labels.map(
      (l: Label): LabelOption => ({
        id: String(l.id),
        label: l.name,
        color: l.color,
      })
    ),
    milestone: milestoneJson.data.milestones.map(
      (m: Milestone): MilestoneOption => ({
        id: String(m.id),
        label: m.title,
      })
    ),
    assignee: userJson.data.users.map(
      (u: User): UserOption => ({
        id: String(u.id),
        label: u.username,
        iconUrl: u.profileImageUrl,
      })
    ),
    writer: userJson.data.users.map(
      (u: User): UserOption => ({
        id: String(u.id),
        label: u.username,
        iconUrl: u.profileImageUrl,
      })
    ),
  } as FetchFilterOptionsResult;
}
