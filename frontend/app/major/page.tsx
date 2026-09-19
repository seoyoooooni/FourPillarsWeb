"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, hasToken, MajorRecommendation, Profile } from "@/lib/api";
import { BackButton } from "@/components/BackButton";

const aptitudeOrder = ["이론·분석", "설계·창작", "실험·연구", "제작·정비", "데이터·디지털", "협업·소통", "서비스·현장", "운영·관리"];
const questions = [
  { text: "더 끌리는 과제는?", kind: "interest", options: [["원리를 분석해 답 찾기", "이론·분석"], ["직접 만들고 고치기", "제작·정비"]] },
  { text: "결과물을 만들 때 더 중요한 것은?", kind: "interest", options: [["새로운 디자인과 아이디어", "설계·창작"], ["정확한 절차와 일정", "운영·관리"]] },
  { text: "더 해보고 싶은 활동은?", kind: "interest", options: [["실험하고 관찰하기", "실험·연구"], ["코딩과 데이터 다루기", "데이터·디지털"]] },
  { text: "사람과 일할 때 더 편한 역할은?", kind: "interest", options: [["의견을 연결하고 협업하기", "협업·소통"], ["고객을 직접 돕고 응대하기", "서비스·현장"]] },
  { text: "문제가 생기면 먼저 하는 일은?", kind: "interest", options: [["자료와 원인부터 파악하기", "이론·분석"], ["현장에서 바로 시험하기", "제작·정비"]] },
  { text: "더 잘 배우는 방식은?", kind: "environment", options: [["강의와 자료로 체계적으로", "이론·분석"], ["실습하며 몸으로 익히기", "제작·정비"]] },
  { text: "선호하는 작업 환경은?", kind: "environment", options: [["팀과 자주 의견을 나누는 환경", "협업·소통"], ["혼자 깊이 집중하는 환경", "실험·연구"]] },
] as const;

export default function MajorPage() {
  const router = useRouter();
  const [result, setResult] = useState<MajorRecommendation>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [profile, setProfile] = useState<Profile>();
  const [profileLoading, setProfileLoading] = useState(true);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [step, setStep] = useState(0);
  const [started, setStarted] = useState(false);

  useEffect(() => {
    if (!hasToken()) { setProfileLoading(false); return; }
    api<Profile>("/api/profile", {}, true)
      .then(setProfile)
      .catch(reason => setError(reason instanceof Error ? reason.message : "프로필을 불러오지 못했습니다."))
      .finally(() => setProfileLoading(false));
  }, []);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!profile || Object.keys(answers).length !== questions.length) return;
    setBusy(true);
    setError("");
    try {
      const interests = preferenceMap("interest");
      const environments = preferenceMap("environment");
      setResult(await api<MajorRecommendation>("/api/fortune/major-recommendations", {
        method: "POST",
        body: JSON.stringify({ birthDate: profile.birthDate, birthTime: profile.birthTime, calendarType: profile.calendarType, leapMonth: profile.leapMonth, interests, environments }),
      }));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "추천 결과를 불러오지 못했습니다.");
    } finally {
      setBusy(false);
    }
  }

  function choose(index: number, axis: string) {
    setAnswers(current => ({ ...current, [index]: axis }));
    if (index < questions.length - 1) window.setTimeout(() => setStep(current => current === index ? current + 1 : current), 220);
  }

  function preferenceMap(kind: "interest" | "environment") {
    const values = Object.fromEntries(aptitudeOrder.map(axis => [axis, 45]));
    questions.forEach((question, index) => { if (question.kind === kind && answers[index]) values[answers[index]] += 35; });
    return Object.fromEntries(Object.entries(values).map(([axis, value]) => [axis, Math.min(100, value)]));
  }

  return <main className={`app-shell wide ${result ? "result-shell" : "form-shell"}`}>
    {profile && !result && !started && <BackButton />}
    {profileLoading ? <div className="loading">프로필을 불러오는 중…</div> : !profile ? <section className="empty-state major-profile-required">
      <h1>프로필 설정이 필요해요</h1><p>학과 추천은 프로필에 저장된 생년월일과 태어난 시를 사용해요.</p>
      <Link className="primary link-button" href={hasToken() ? "/profile" : "/login"}>{hasToken() ? "프로필 설정하기" : "로그인하기"}</Link>
    </section> : !result ? <form className="major-form major-wizard maui-form" onSubmit={submit}>
      {!started ? <><section className="major-question-intro"><h1>사주와 약간의 질문으로<br />잘 맞는 학과를 추천해 드릴게요.</h1></section><div className="wizard-actions intro-action"><button type="button" className="primary" onClick={() => setStarted(true)}>좋아요!</button></div></> : <>
      <section className="preference-questions wizard-question">
        <div className="wizard-progress" aria-label={`질문 진행률 ${step + 1}/${questions.length}`}><i><b style={{ width: `${((step + 1) / questions.length) * 100}%` }} /></i></div>
        <fieldset key={questions[step].text}><legend>{questions[step].text}</legend><div>{questions[step].options.map(([label, axis]) => <label key={axis} className={answers[step] === axis ? "selected" : ""}>
          <input type="radio" name={`question-${step}`} value={axis} checked={answers[step] === axis} onChange={() => choose(step, axis)} />{label}
        </label>)}</div></fieldset>
      </section>
      {error && <p className="error">{error}</p>}
      <div className="wizard-actions"><button type="button" className="secondary" disabled={step === 0} onClick={() => setStep(step - 1)}>이전</button>
        {step < questions.length - 1 ? <button type="button" className="primary" disabled={!answers[step]} onClick={() => setStep(step + 1)}>다음</button> : <button className="primary" disabled={busy || !answers[step]}>{busy ? "분석 중…" : "추천 결과 보기"}</button>}</div>
      </>}
    </form> : <><BackButton onClick={() => router.push("/")} label="메인 화면으로 이동" /><MajorResults result={result} /></>}
  </main>;
}

function MajorResults({ result }: { result: MajorRecommendation }) {
  return <section className="major-results">
    <header className="major-result-hero">
      <span className="result-meta">나의 전공 탐색 성향</span>
      <h1>{result.studentType}</h1>
    </header>
    <article className="trait-card">
      <h2>나의 8가지 전공 적성</h2>
      {aptitudeOrder.map(axis => <div className="trait-row aptitude-row" key={axis}>
        <span>{axis}</span>
        <i><b style={{ width: `${result.aptitudes[axis] ?? 0}%` }} /></i>
        <em>{result.aptitudes[axis] ?? 0}</em>
      </div>)}
    </article>
    <section className="department-section">
      <div className="section-heading"><h2>나와 잘 맞는 학과 TOP 3</h2></div>
      <div className="department-results">
        {result.recommendations.map((department, index) => <article className="department-result" key={department.department}>
          <span className="department-rank">{index + 1}</span>
          <div>
            <span className="department-role">{department.role}</span>
            <h3>{department.department}</h3>
            <ul className="department-reasons">{department.reasons.map(reason => <li key={reason}>{reason}</li>)}</ul>
            {department.admissionNote && <p className="department-admission">{department.admissionNote}</p>}
          </div>
          <strong>{department.score}<small>점</small></strong>
        </article>)}
      </div>
      <a className="department-source department-source-all" href="https://opmajors.inhatc.ac.kr/ipsi/579/subview.do" target="_blank" rel="noopener noreferrer">인하공전 전체 학과정보 보기 ↗</a>
    </section>
    <p className="major-disclaimer">{result.disclaimer}</p>
  </section>;
}
