"use client";

import { CacheProvider } from "@emotion/react";
import type { ReactNode } from "react";
import { emotionCache } from "./emotion-cache";

export default function EmotionProvider({ children }: { children: ReactNode }) {
  return <CacheProvider value={emotionCache}>{children}</CacheProvider>;
}
