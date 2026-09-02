"use client";
import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login, signup } from "@/lib/api";

export function AuthForm({ mode }: { mode: "login" | "signup" }) {
  const router = useRouter(); const [busy, setBusy] = useState(false); const [error, setError] = useState("");
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError("");
    const data = new FormData(event.currentTarget); const email = String(data.get("email")); const password = String(data.get("password"));
    if (mode === "signup" && password !== String(data.get("confirm"))) { setError("비밀번호가 서로 다릅니다."); setBusy(false); return; }
    try { if (mode === "signup") await signup(email, password); await login(email, password); router.push(mode === "signup" ? "/profile" : "/"); }
    catch (e) { setError(e instanceof Error ? e.message : "요청에 실패했습니다."); } finally { setBusy(false); }
  }
  return <form className="center-form maui-form" onSubmit={submit}>
    <label><strong>이메일</strong><input name="email" type="email" placeholder="user@example.com" required /></label>
    <label><strong>비밀번호</strong><input name="password" type="password" placeholder="8자 이상 입력" minLength={8} required /></label>
    {mode === "signup" && <label><strong>비밀번호 확인</strong><input name="confirm" type="password" placeholder="비밀번호 다시 입력" minLength={8} required /></label>}
    {error && <p className="error">{error}</p>}<button className="primary" disabled={busy}>{busy ? "처리 중…" : mode === "login" ? "로그인" : "회원가입"}</button>
    {mode === "login" && <Link className="text-link" href="/signup">회원가입</Link>}
  </form>;
}
