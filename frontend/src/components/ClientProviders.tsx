// 클라이언트 전용 로직은 별도 Client Component로 분리
// EmotionProvider, ThemeProvider, GlobalStyles와 같이 내부에서 브라우저 API를 쓰거나 useEffect를 사용하는 부분만 분리
"use client";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { PropsWithChildren, useState } from "react";

import EmotionProvider from "../styles/emotionProvider";
import ThemeProvider from "@components/theme/ThemeProvider";
import GlobalStyles from "@/styles/globalStyles";

export default function ClientProviders({
  children,
}: {
  children: React.ReactNode;
}) {
  const [client] = useState(() => new QueryClient());

  return (
    <QueryClientProvider client={client}>
      <EmotionProvider>
        <ThemeProvider>
          <GlobalStyles />
          {children}
          {process.env.NODE_ENV === "development" && <ReactQueryDevtools />}
        </ThemeProvider>
      </EmotionProvider>
    </QueryClientProvider>
  );
}
