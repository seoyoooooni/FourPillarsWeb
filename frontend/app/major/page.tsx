"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { api, hasToken, MajorRecommendation, Profile } from "@/lib/api";
import { BackButton } from "@/components/BackButton";

const aptitudeOrder = ["이론·분석", "설계·창작", "실험·연구", "제작·정비", "데이터·디지털", "협업·소통", "서비스·현장", "운영·관리"];
type Scores = Record<string, number>;
type Question = { text: string; kind: "interest" | "environment"; options: { label: string; scores: Scores }[] };

const questions: Question[] = [
  { text: "더 끌리는 활동은?", kind: "interest", options: [
    { label: "원리를 분석하기", scores: { "이론·분석": 100, "데이터·디지털": 85, "실험·연구": 70 } },
    { label: "직접 만들기", scores: { "설계·창작": 100, "제작·정비": 90, "데이터·디지털": 60 } },
    { label: "사람과 소통하기", scores: { "협업·소통": 100, "서비스·현장": 90, "운영·관리": 65 } },
    { label: "새로운 것을 탐구하기", scores: { "실험·연구": 100, "이론·분석": 80, "데이터·디지털": 75 } },
  ] },
  { text: "더 편한 학습 방식은?", kind: "environment", options: [
    { label: "이론을 체계적으로 배우기", scores: { "이론·분석": 100, "실험·연구": 80, "운영·관리": 65 } },
    { label: "실습하며 익히기", scores: { "제작·정비": 100, "서비스·현장": 75, "설계·창작": 70 } },
    { label: "팀으로 함께하기", scores: { "협업·소통": 100, "서비스·현장": 80, "운영·관리": 70 } },
    { label: "혼자 깊이 집중하기", scores: { "실험·연구": 100, "데이터·디지털": 85, "이론·분석": 75 } },
  ] },
];

export default function MajorPage() {
  const router = useRouter();
  const [result, setResult] = useState<MajorRecommendation>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [profile, setProfile] = useState<Profile>();
  const [profileLoading, setProfileLoading] = useState(true);
  const [answers, setAnswers] = useState<Record<number, number>>({});
  const [step, setStep] = useState(0);
  const [questionMode, setQuestionMode] = useState(false);
  const [personalized, setPersonalized] = useState(false);

  useEffect(() => {
    if (!hasToken()) { setProfileLoading(false); return; }
    api<Profile>("/api/profile", {}, true)
      .then(setProfile)
      .catch(reason => setError(reason instanceof Error ? reason.message : "프로필을 불러오지 못했습니다."))
      .finally(() => setProfileLoading(false));
  }, []);

  async function recommend(customize: boolean) {
    if (!profile) return;
    setBusy(true);
    setError("");
    try {
      const preferences = customize ? preferenceMaps() : {};
      const nextResult = await api<MajorRecommendation>("/api/fortune/major-recommendations", {
        method: "POST",
        body: JSON.stringify({ birthDate: profile.birthDate, birthTime: profile.birthTime, calendarType: profile.calendarType, leapMonth: profile.leapMonth, ...preferences }),
      });
      setPersonalized(customize);
      setQuestionMode(false);
      setResult(nextResult);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "추천 결과를 불러오지 못했습니다.");
    } finally { setBusy(false); }
  }

  async function submitQuestions(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (Object.keys(answers).length !== questions.length) return;
    await recommend(true);
  }

  function choose(index: number, optionIndex: number) {
    setAnswers(current => ({ ...current, [index]: optionIndex }));
    if (index < questions.length - 1) window.setTimeout(() => setStep(current => current === index ? current + 1 : current), 220);
  }

  function preferenceMaps() {
    const maps = { interests: neutralScores(), environments: neutralScores() };
    questions.forEach((question, index) => {
      const answer = answers[index];
      if (answer === undefined) return;
      const target = question.kind === "interest" ? maps.interests : maps.environments;
      Object.assign(target, question.options[answer].scores);
    });
    return maps;
  }

  function startQuestions() {
    setResult(undefined); setQuestionMode(true); setStep(0); setAnswers({}); setError("");
  }

  return <main className={`app-shell wide ${result ? "result-shell" : "form-shell"}`}>
    {profile && !result && !questionMode && <BackButton />}
    {profileLoading ? <div className="loading">프로필을 불러오는 중…</div> : !profile ? <section className="empty-state major-profile-required">
      <img className="empty-state-mascot" src="/images/mascot-guide-talking.png" alt="" aria-hidden="true" />
      <h1>프로필 설정이 필요해요</h1><p>학과 추천은 프로필에 저장된 생년월일과 태어난 시를 사용해요.</p>
      <Link className="primary link-button" href={hasToken() ? "/profile" : "/login"}>{hasToken() ? "프로필 설정하기" : "로그인하기"}</Link>
    </section> : result ? <><BackButton onClick={() => router.push("/")} label="메인 화면으로 이동" /><MajorResults result={result} personalized={personalized} onPersonalize={startQuestions} /></> : questionMode ? <form className="major-form major-wizard maui-form" onSubmit={submitQuestions}>
      <section className="preference-questions wizard-question">
        <div className="wizard-progress" aria-label={`질문 진행률 ${step + 1}/${questions.length}`}><i><b style={{ width: `${((step + 1) / questions.length) * 100}%` }} /></i></div>
        <div className="wizard-question-body"><fieldset key={questions[step].text}><legend>{questions[step].text}</legend><div>{questions[step].options.map((option, optionIndex) => <label key={option.label} className={answers[step] === optionIndex ? "selected" : ""}>
          <input type="radio" name={`question-${step}`} checked={answers[step] === optionIndex} onChange={() => choose(step, optionIndex)} />{option.label}
        </label>)}</div></fieldset></div>
      </section>
      {error && <p className="error major-error">{error}</p>}
      <div className="wizard-actions"><button type="button" className="secondary" onClick={() => step === 0 ? setQuestionMode(false) : setStep(step - 1)}>{step === 0 ? "취소" : "이전"}</button>
        {step < questions.length - 1 ? <button type="button" className="primary" disabled={answers[step] === undefined} onClick={() => setStep(step + 1)}>다음</button> : <button className="primary" disabled={busy || answers[step] === undefined}>{busy ? "분석 중…" : "다시 추천받기"}</button>}</div>
    </form> : <section className="major-form major-wizard maui-form">
      <section className="major-question-intro"><div className="major-intro-aura"><img src="/images/mascot-major-clipboard.png" alt="학과 탐색을 안내하는 여우 마스코트" /></div><div><h1>내 사주에 맞는 학과를<br />바로 찾아볼까요?</h1></div></section>
      {error && <p className="error major-error">{error}</p>}
      <div className="wizard-actions intro-action"><button type="button" className="primary" disabled={busy} onClick={() => recommend(false)}>{busy ? "사주를 분석하는 중…" : "내 사주로 학과 찾기"}</button></div>
    </section>}
  </main>;
}

function neutralScores(): Scores { return Object.fromEntries(aptitudeOrder.map(axis => [axis, 45])); }

function MajorResults({ result, personalized, onPersonalize }: { result: MajorRecommendation; personalized: boolean; onPersonalize: () => void }) {
  const calculationNotice = personalized
    ? "사주 적성 70%, 관심 분야 20%, 학습 환경 10%로 계산한 탐색 점수예요. 적합 확률이나 합격 가능성은 아니며, 지원 전 모집요강을 확인해 주세요."
    : "생년월일과 태어난 시로 본 사주 적성 100% 기반의 탐색 점수예요. 적합 확률이나 합격 가능성은 아니며, 지원 전 모집요강을 확인해 주세요.";
  const typeWords = result.studentType.split(" ");
  const typeLead = typeWords.slice(0, -2).join(" ");
  const typeName = typeWords.slice(-2).join(" ");
  return <section className="major-results">
    <header className="major-result-hero"><span className="result-meta">{personalized ? "사주와 내 선호를 함께 본 전공 성향" : "내 사주로 본 전공 탐색 성향"}</span><h1>{typeLead}<br />{typeName}</h1></header>
    <article className="trait-card"><h2>나의 8가지 전공 적성</h2>{aptitudeOrder.map(axis => <div className="trait-row aptitude-row" key={axis}><span>{axis}</span><i><b style={{ width: `${result.aptitudes[axis] ?? 0}%` }} /></i><em>{result.aptitudes[axis] ?? 0}</em></div>)}</article>
    <section className="department-section"><div className="section-heading"><h2>나와 잘 맞는 학과 TOP 3</h2></div><div className="department-results">
      {result.recommendations.map((department, index) => <article className="department-result" key={department.department}><span className="department-rank">{index + 1}</span><div><span className="department-role">{department.role}</span><h3>{department.department}</h3><ul className="department-reasons">{department.reasons.map(reason => <li key={reason}>{reason}</li>)}</ul>{department.admissionNote && <p className="department-admission">{department.admissionNote}</p>}</div><strong>{department.score}<small>점</small></strong></article>)}
    </div><a className="department-source department-source-all" href="https://opmajors.inhatc.ac.kr/ipsi/579/subview.do" target="_blank" rel="noopener noreferrer">인하공전 전체 학과정보 보기 ↗</a></section>
    {!personalized && <section className="major-personalize"><h2>조금 더 나답게 보고 싶나요?</h2><button type="button" className="primary" onClick={onPersonalize}>질문 2개로 내 관심사까지 반영해 다시 추천받기</button></section>}
    <p className="major-disclaimer">{calculationNotice}</p>
  </section>;
}
