"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { login, signup } from "@/lib/api";
import { BackButton } from "@/components/BackButton";

export function AuthForm({ mode }: { mode: "login" | "signup" }) {
  const router = useRouter();
  const [step, setStep] = useState<"loginId" | "password">("loginId");
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const validLoginId = /^[a-z][a-z0-9_]{3,19}$/.test(loginId);
  const validPassword = password.length >= 8;
  const canSubmit = mode === "login" ? validPassword : validPassword && confirm === password;

  function goBack() {
    if (step === "password") {
      setStep("loginId"); setPassword(""); setConfirm(""); setError(""); setPasswordVisible(false);
    } else router.back();
  }

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError("");
    if (step === "loginId") {
      if (!validLoginId) { setError("아이디는 영문자로 시작하는 4~20자의 영문, 숫자, 밑줄(_) 조합으로 입력해 주세요."); return; }
      setStep("password"); return;
    }
    if (!validPassword) { setError("비밀번호는 8자 이상 입력해 주세요."); return; }
    if (mode === "signup" && password !== confirm) { setError("비밀번호가 서로 다릅니다."); return; }
    setBusy(true);
    try { if (mode === "signup") await signup(loginId, password); await login(loginId, password); router.push(mode === "signup" ? "/profile" : "/"); }
    catch (e) { setError(e instanceof Error ? e.message : "요청에 실패했습니다."); } finally { setBusy(false); }
  }

  return <section className="auth-panel" aria-label={mode === "login" ? "로그인" : "회원가입"}>
    <BackButton onClick={goBack} />
    <form className="auth-step-form" onSubmit={submit} noValidate>
      <div className="auth-fields">
        {step === "loginId" ? <label className={`auth-input ${error ? "error" : ""}`}>
          <span className="sr-only">아이디</span><input value={loginId} placeholder="아이디" autoComplete="username" autoCapitalize="none" spellCheck={false} autoFocus onChange={(e) => { setLoginId(e.target.value.toLowerCase()); setError(""); }} />{validLoginId && <CheckIcon />}
        </label> : <>
          <label className={`auth-input ${error && !validPassword ? "error" : ""}`}>
            <span className="sr-only">비밀번호</span><input type={passwordVisible ? "text" : "password"} value={password} placeholder={mode === "signup" ? "비밀번호 (8자 이상)" : "비밀번호"} autoComplete={mode === "signup" ? "new-password" : "current-password"} autoFocus onChange={(e) => { setPassword(e.target.value); setError(""); }} />
            <button className="auth-visibility" type="button" onClick={() => setPasswordVisible((value) => !value)} aria-label={passwordVisible ? "비밀번호 숨기기" : "비밀번호 보기"}><EyeIcon crossed={passwordVisible} /></button>
          </label>
          {mode === "signup" && <label className={`auth-input ${error && confirm !== password ? "error" : ""}`}>
            <span className="sr-only">비밀번호 확인</span><input type={passwordVisible ? "text" : "password"} value={confirm} placeholder="비밀번호 확인" autoComplete="new-password" onChange={(e) => { setConfirm(e.target.value); setError(""); }} />{confirm && confirm === password && <CheckIcon />}
          </label>}
          <label className="auth-input auth-confirmed-email">
            <span className="sr-only">아이디 수정</span><input value={loginId} autoComplete="username" autoCapitalize="none" spellCheck={false} onChange={(e) => setLoginId(e.target.value.toLowerCase())} />{validLoginId && <CheckIcon />}
          </label>
        </>}
        {error && <p className="auth-error" role="alert">{error}</p>}
      </div>
      <div className="auth-actions">
        <button className="auth-submit" disabled={busy || (step === "loginId" ? !validLoginId : !canSubmit)}>{busy ? "처리 중…" : step === "loginId" ? "다음" : mode === "login" ? "로그인" : "가입하기"}</button>
        {mode === "login" && step === "loginId" && <Link href="/signup">처음이신가요? <strong>회원가입</strong></Link>}
        {!(mode === "login" && step === "loginId") && <span className="auth-action-spacer" aria-hidden="true" />}
      </div>
    </form>
  </section>;
}

function CheckIcon() { return <svg className="auth-check" viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4.5 4.5L19 7" /></svg>; }
function EyeIcon({ crossed }: { crossed: boolean }) { return <svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.5 12s3.5-6 9.5-6 9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z"/><circle cx="12" cy="12" r="2.5"/>{crossed && <path d="m4 4 16 16"/>}</svg>; }
