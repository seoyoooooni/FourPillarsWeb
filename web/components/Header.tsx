"use client";
import { useRouter } from "next/navigation";

export function Header({ title: _title }: { title?: string }) {
  const router = useRouter();
  return <header className="page-header"><button className="icon-button" onClick={() => router.back()} aria-label="뒤로 가기">‹</button></header>;
}
