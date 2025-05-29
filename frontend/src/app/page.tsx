"use client";

import { redirect } from "next/navigation";

export default function HomePage() {
  // 방문 즉시 /issues로 리다이렉트 (307 Temporary)
  redirect("/issues");
}
