"use client";

import { FormEvent, useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { BirthDateField, BirthTimeField } from "@/components/BirthPickers";
import { api, hasToken, MajorRecommendation, Profile } from "@/lib/api";

const traitOrder = ["분석", "창의", "소통", "실행", "탐구"];

export default function MajorPage() {
  const [result, setResult] = useState<MajorRecommendation>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [date, setDate] = useState("2000-01-01");
  const [time, setTime] = useState("12:00");
  const [calendar, setCalendar] = useState<"SOLAR" | "LUNAR">("SOLAR");
  const [leap, setLeap] = useState(false);

  useEffect(() => {
    if (!hasToken()) return;
    api<Profile>("/api/profile", {}, true)
      .then(profile => {
        setDate(profile.birthDate);
        setTime(profile.birthTime.slice(0, 5));
        setCalendar(profile.calendarType);
        setLeap(profile.leapMonth);
      })
      .catch(() => {});
  }, []);

  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      setResult(await api<MajorRecommendation>("/api/fortune/major-recommendations", {
        method: "POST",
        body: JSON.stringify({ birthDate: date, birthTime: `${time}:00`, calendarType: calendar, leapMonth: leap }),
      }));
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : "추천 결과를 불러오지 못했습니다.");
    } finally {
      setBusy(false);
    }
  }

  return <main className={`app-shell wide ${result ? "result-shell" : "form-shell"}`}>
    <Header title="학부 추천" />
    {!result ? <form className="major-form maui-form" onSubmit={submit}>
      <div className="major-intro">
        <h1>나와 잘 맞는 학부 찾기</h1>
        <p>사주에 나타난 성향을 다섯 가지 학습 유형으로 살펴봐요.</p>
      </div>
      <div className="profile-row"><strong>생년월일</strong><BirthDateField value={date} calendar={calendar} leap={leap} onChange={(value, type, isLeap) => { setDate(value); setCalendar(type); setLeap(isLeap); }} /></div>
      <div className="profile-row"><strong>태어난 시</strong><BirthTimeField value={time} onChange={setTime} /></div>
      {error && <p className="error">{error}</p>}
      <button className="primary" disabled={busy}>{busy ? "분석 중…" : "추천 학부 알아보기"}</button>
    </form> : <MajorResults result={result} />}
  </main>;
}

function MajorResults({ result }: { result: MajorRecommendation }) {
  return <section className="major-results">
    <header className="major-result-hero">
      <span className="result-meta">나의 대학생활 유형</span>
      <h1>{result.studentType}</h1>
      <p>사주에 나타난 기질을 학습과 전공 탐색의 관점으로 풀어봤어요.</p>
    </header>
    <article className="trait-card">
      <h2>나의 성향</h2>
      {traitOrder.map(trait => <div className="trait-row" key={trait}>
        <span>{trait}</span>
        <i><b style={{ width: `${result.traits[trait] ?? 0}%` }} /></i>
        <em>{result.traits[trait] ?? 0}</em>
      </div>)}
    </article>
    <section className="faculty-section">
      <div className="section-heading"><h2>가장 잘 맞는 학부</h2></div>
      {result.recommendations.map(recommendation => <article className="faculty-card" key={recommendation.faculty}>
        <div className="faculty-copy">
          <h3>{recommendation.faculty}</h3>
          <ul>{recommendation.reasons.map(reason => <li key={reason}>{reason}</li>)}</ul>
          {recommendation.departments.length > 0 && <div className="department-list">
            {recommendation.departments.map(department => <span key={department.department}>{department.department}</span>)}
          </div>}
        </div>
        <strong>{recommendation.score}<small>%</small></strong>
      </article>)}
    </section>
    <p className="major-disclaimer">{result.disclaimer}</p>
  </section>;
}
