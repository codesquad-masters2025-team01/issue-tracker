export interface Issue {
  id: number;
  title: string;
  createdAt: string;
  updatedAt: string;
  writerId: number;
  writerName: string;
  writerProfileImageUrl: string;
  labels?: LabelType[];
  milestone?: { title: string };
  assignees?: string[];
  state: "open" | "closed";
}

export interface LabelType {
  id: number;
  name: string;
  color: string;
  textColor: string;
}