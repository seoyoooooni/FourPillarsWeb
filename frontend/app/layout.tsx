import type { Metadata, Viewport } from "next";
import "./styles/colors.css";
import "./styles/globals.css";
import "./styles/fidelity.css";
import "./styles/forms-fidelity.css";
import "./styles/pickers.css";
import "./styles/campus-theme.css";
import "./styles/study-tarot.css";
import "./styles/tarot-shuffle.css";
import "./styles/result-tarot.css";
import "./styles/tarot-tweaks.css";
import "./styles/drawer-menu.css";
import "./styles/team.css";
import "./styles/buttons.css";

export const metadata: Metadata = {
  title: "사주",
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
