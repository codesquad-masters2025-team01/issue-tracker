import type { SelectedFilters } from "@/stores/useIssueFilterStore";

export function buildIssueQuery(filters: SelectedFilters) {
  const qs = new URLSearchParams();
  if (filters.label) qs.append("labels", filters.label);
  if (filters.assignee) qs.append("assignees", filters.assignee);
  if (filters.milestone) qs.append("milestone", filters.milestone);
  if (filters.writer) qs.append("author", filters.writer);
  return qs.toString(); // "" 또는 "labels=2&assignees=7"
}
