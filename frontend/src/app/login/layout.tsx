"use client";

import LoginProviders from "@components/login/LoginProviders";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body suppressHydrationWarning>
        <LoginProviders>
          {/* <Header /> */}
          {children}
        </LoginProviders>
      </body>
    </html>
  );
}
