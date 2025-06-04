"use client";

import { Global, css } from "@emotion/react";
import reset from "emotion-reset";

const base = css`
  ${reset}
  *,
  *::before,
  *::after {
    box-sizing: border-box;
    transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease,
      fill 0.2s ease, stroke 0.2s ease;
  }
  html,
  body {
    margin: 0;
    padding: 0;
  }
  body {
    font-family: "Pretendard", -apple-system, BlinkMacSystemFont, "Segoe UI",
      Roboto, Oxygen, Ubuntu, Cantarell, "Open Sans", "Helvetica Neue",
      sans-serif;
    background: var(--bg);
    color: var(--fg);
  }
  a {
    color: inherit;
    text-decoration: none;
  }

  .your-scroll-container::-webkit-scrollbar {
    display: none;
  }
`;

export default function GlobalStyles() {
  return <Global styles={base} />;
}
