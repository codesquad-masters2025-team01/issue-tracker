"use client";

import Header from "@components/header/Header";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body suppressHydrationWarning>
        <Header />
        {children}
      </body>
    </html>
  );
}
