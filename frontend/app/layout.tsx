import type { Metadata, Viewport } from "next";
import "./globals.css";
import "./fidelity.css";
import "./forms-fidelity.css";
import "./pickers.css";
import "./campus-theme.css";
import "./decision.css";
import "./buttons.css";

export const metadata: Metadata = {
  title: "사주, 마음을 비추다",
  description: "생년월일과 출생시간으로 살펴보는 나의 사주와 오늘의 흐름",
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
  themeColor: "#f5f8fc",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="ko"><body>{children}</body></html>;
}
