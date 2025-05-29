import "@emotion/react";
import type { ThemeType } from "@/styles/theme";

declare module "@emotion/react" {
  export interface Theme extends ThemeType {}
}
