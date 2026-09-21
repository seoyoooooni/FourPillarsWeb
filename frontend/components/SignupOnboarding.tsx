"use client";

import { FormEvent, useState } from "react";
import Link from "next/link";
import { BirthDateField, BirthTimeField } from "@/components/BirthPickers";
import { api, login, signup } from "@/lib/api";

const steps = ["welcome", "name", "email", "password", "confirm", "birthDate", "birthTime", "gender"] as const;
type Step = typeof steps[number];

export function SignupOnboarding() {
  const [index, setIndex] = useState(0);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [name, setName] = useState("");
  const [birthDate, setBirthDate] = useState("2000-01-01");
  const [birthTime, setBirthTime] = useState("12:00");
  const [calendarType, setCalendarType] = useState<"SOLAR" | "LUNAR">("SOLAR");
  const [leapMonth, setLeapMonth] = useState(false);
  const [gender, setGender] = useState<"MALE" | "FEMALE">();
  const [visible, setVisible] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const step: Step = steps[index];

  function validationMessage() {
    if (step === "email" && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return "올바른 이메일 주소를 입력해 주세요.";
    if (step === "password" && password.length < 8) return "비밀번호는 8자 이상 입력해 주세요.";
    if (step === "confirm" && confirm !== password) return "비밀번호가 서로 다릅니다.";
    if (step === "name" && !name.trim()) return "이름을 입력해 주세요.";
    if (step === "birthDate" && !birthDate) return "생년월일을 선택해 주세요.";
    if (step === "birthTime" && !birthTime) return "태어난 시간을 선택해 주세요.";
    if (step === "gender" && !gender) return "성별을 선택해 주세요.";
    return "";
  }

  async function next(event: FormEvent) {
    event.preventDefault();
    const message = validationMessage();
    if (message) { setError(message); return; }
    if (index < steps.length - 1) { setError(""); setIndex(value => value + 1); return; }
    setBusy(true); setError("");
    try {
      await signup(email, password);
      await login(email, password);
      await api("/api/profile", {
        method: "PUT",
        body: JSON.stringify({
          displayName: name.trim(), birthDate, birthTime: `${birthTime}:00`,
          calendarType, leapMonth, gender,
        }),
      }, true);
      window.location.replace("/");
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "회원가입을 완료하지 못했습니다.");
    } finally { setBusy(false); }
  }

  return <section className="auth-panel signup-onboarding" aria-label="회원가입 및 프로필 설정">
    <div className="signup-progress" aria-label={`회원가입 진행률 ${index + 1}/${steps.length}`}><i><b style={{ width: `${((index + 1) / steps.length) * 100}%` }} /></i></div>
    <form className="signup-step-form" onSubmit={next} noValidate>
      <div className="signup-step-content">
        {step === "welcome" && <div className="signup-welcome-scene">
          <div className="signup-welcome-aura"><img className="signup-welcome-mascot" src="/images/mascot-guide-talking.png" alt="가입 과정을 안내하는 여우 마스코트" /></div>
          <h1>안녕하세요!</h1>
        </div>}
        {step === "email" && <StepInput title="이메일을 입력해 주세요" error={error}><input type="email" value={email} autoComplete="email" autoFocus placeholder="name@example.com" onChange={e => { setEmail(e.target.value); setError(""); }} /></StepInput>}
        {step === "password" && <StepInput title="비밀번호를 만들어 주세요" hint="8자 이상 입력해 주세요." error={error}><PasswordInput value={password} visible={visible} autoFocus onChange={value => { setPassword(value); setError(""); }} onToggle={() => setVisible(value => !value)} /></StepInput>}
        {step === "confirm" && <StepInput title="비밀번호를 한 번 더 입력해 주세요" error={error}><PasswordInput value={confirm} visible={visible} autoFocus onChange={value => { setConfirm(value); setError(""); }} onToggle={() => setVisible(value => !value)} /></StepInput>}
        {step === "name" && <StepInput title="이름을 알려 주세요" error={error}><input value={name} autoComplete="name" autoFocus placeholder="이름" maxLength={50} onChange={e => { setName(e.target.value); setError(""); }} /></StepInput>}
        {step === "birthDate" && <StepInput title="생년월일을 선택해 주세요" error={error}><div className="signup-picker"><BirthDateField value={birthDate} calendar={calendarType} leap={leapMonth} onChange={(date, calendar, leap) => { setBirthDate(date); setCalendarType(calendar); setLeapMonth(leap); setError(""); }} /></div></StepInput>}
        {step === "birthTime" && <StepInput title="태어난 시간을 선택해 주세요" error={error}><div className="signup-picker"><BirthTimeField value={birthTime} onChange={value => { setBirthTime(value); setError(""); }} /></div></StepInput>}
        {step === "gender" && <StepInput title="성별을 선택해 주세요" error={error}><div className="signup-gender"><button type="button" className={gender === "MALE" ? "selected" : ""} onClick={() => { setGender("MALE"); setError(""); }}>남성</button><button type="button" className={gender === "FEMALE" ? "selected" : ""} onClick={() => { setGender("FEMALE"); setError(""); }}>여성</button></div></StepInput>}
      </div>
      <div className="auth-actions"><button className="auth-submit" disabled={busy}>{busy ? "가입하는 중…" : step === "welcome" ? "안녕!" : index === steps.length - 1 ? "가입 완료" : "다음"}</button>{step === "welcome" ? <Link href="/login">이미 계정이 있나요? <strong>로그인</strong></Link> : <span className="auth-action-spacer" />}</div>
    </form>
  </section>;
}

function StepInput({ title, hint, error, children }: { title: string; hint?: string; error?: string; children?: React.ReactNode }) {
  return <div className={`signup-field ${error ? "invalid" : ""}`}><h1>{title}</h1>{hint && <p>{hint}</p>}<div className="signup-control">{children}</div>{error && <p className="signup-inline-error" role="alert">{error}</p>}</div>;
}

function PasswordInput({ value, visible, autoFocus, onChange, onToggle }: { value: string; visible: boolean; autoFocus?: boolean; onChange: (value: string) => void; onToggle: () => void }) {
  return <label className="auth-input"><input type={visible ? "text" : "password"} value={value} autoComplete="new-password" autoFocus={autoFocus} placeholder="비밀번호" onChange={e => onChange(e.target.value)} /><button className="auth-visibility" type="button" onClick={onToggle} aria-label={visible ? "비밀번호 숨기기" : "비밀번호 보기"}>◉</button></label>;
}
