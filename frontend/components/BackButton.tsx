"use client";

import { useRouter } from "next/navigation";

export function BackButton({ onClick, label = "뒤로 가기" }: { onClick?: () => void; label?: string }) {
  const router = useRouter();
  return <button className="app-back-button" type="button" onClick={onClick ?? (() => router.back())} aria-label={label}>‹</button>;
}
