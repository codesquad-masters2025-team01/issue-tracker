/** 옵션 공통 타입 */
export interface Option {
  id: string;
  label: string;
  color?: string;
  iconUrl?: string;
}

/** 필터 옵션 전체 묶음 */
export interface FilterOptions {
  label: Option[];
  milestone: Option[];
  assignee: Option[];
  writer: Option[];
}
