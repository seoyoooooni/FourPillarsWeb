"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login, signup } from "@/lib/api";

export function AuthForm({ mode }: { mode: "login" | "signup" }) {
  const router = useRouter();
  const [step, setStep] = useState<"email" | "password">("email");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const validEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  const validPassword = password.length >= 8;
  const canSubmit = mode === "login" ? validPassword : validPassword && confirm === password;

  function goBack() {
    if (step === "password") {
      setStep("email"); setPassword(""); setConfirm(""); setError(""); setPasswordVisible(false);
    } else router.back();
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError("");
    if (step === "email") {
      if (!validEmail) { setError("올바른 이메일 주소를 입력해 주세요."); return; }
      setStep("password"); return;
    }
    if (!validPassword) { setError("비밀번호는 8자 이상 입력해 주세요."); return; }
    if (mode === "signup" && password !== confirm) { setError("비밀번호가 서로 다릅니다."); return; }
    setBusy(true);
    try { if (mode === "signup") await signup(email, password); await login(email, password); router.push(mode === "signup" ? "/profile" : "/"); }
    catch (e) { setError(e instanceof Error ? e.message : "요청에 실패했습니다."); } finally { setBusy(false); }
  }

  return <section className="auth-panel" aria-label={mode === "login" ? "로그인" : "회원가입"}>
    <button className="auth-back" type="button" onClick={goBack} aria-label="뒤로 가기">‹</button>
    <form className="auth-step-form" onSubmit={submit} noValidate>
      <div className="auth-fields">
        {step === "email" ? <label className={`auth-input ${error ? "error" : ""}`}>
          <span className="sr-only">이메일</span><input type="email" value={email} placeholder="이메일" autoComplete="email" autoFocus onChange={(e) => { setEmail(e.target.value); setError(""); }} />{validEmail && <CheckIcon />}
        </label> : <>
          <label className={`auth-input ${error && !validPassword ? "error" : ""}`}>
            <span className="sr-only">비밀번호</span><input type={passwordVisible ? "text" : "password"} value={password} placeholder={mode === "signup" ? "비밀번호 (8자 이상)" : "비밀번호"} autoComplete={mode === "signup" ? "new-password" : "current-password"} autoFocus onChange={(e) => { setPassword(e.target.value); setError(""); }} />
            <button className="auth-visibility" type="button" onClick={() => setPasswordVisible((value) => !value)} aria-label={passwordVisible ? "비밀번호 숨기기" : "비밀번호 보기"}><EyeIcon crossed={passwordVisible} /></button>
          </label>
          {mode === "signup" && <label className={`auth-input ${error && confirm !== password ? "error" : ""}`}>
            <span className="sr-only">비밀번호 확인</span><input type={passwordVisible ? "text" : "password"} value={confirm} placeholder="비밀번호 확인" autoComplete="new-password" onChange={(e) => { setConfirm(e.target.value); setError(""); }} />{confirm && confirm === password && <CheckIcon />}
          </label>}
          <label className="auth-input auth-confirmed-email">
            <span className="sr-only">이메일 수정</span><input type="email" value={email} autoComplete="email" onChange={(e) => setEmail(e.target.value)} />{validEmail && <CheckIcon />}
          </label>
        </>}
        {error && <p className="auth-error" role="alert">{error}</p>}
      </div>
      <div className="auth-actions">
        <button className="auth-submit" disabled={busy || (step === "email" ? !validEmail : !canSubmit)}>{busy ? "처리 중…" : step === "email" ? "다음" : mode === "login" ? "로그인" : "가입하기"}</button>
        {mode === "login" && step === "email" && <Link href="/signup">처음이신가요? <strong>회원가입</strong></Link>}
        {!(mode === "login" && step === "email") && <span className="auth-action-spacer" aria-hidden="true" />}
      </div>
    </form>
  </section>;
}

function CheckIcon() { return <svg className="auth-check" viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4.5 4.5L19 7" /></svg>; }
function EyeIcon({ crossed }: { crossed: boolean }) { return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/>{crossed && <path d="m4 4 16 16"/>}</svg>; }
