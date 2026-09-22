"use client";

import { FormEvent, useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { BirthDateField, BirthTimeField } from "@/components/BirthPickers";
import { api, Calculation, hasToken, Profile } from "@/lib/api";
import { analyzeTeam, TeamResult } from "@/lib/team-compatibility";

type Member = { id: number; name: string; birthDate: string; birthTime: string; calendarType: "SOLAR" | "LUNAR"; leapMonth: boolean };

let nextId = 3;
const newMember = (id: number, name = ""): Member => ({ id, name, birthDate: "2000-01-01", birthTime: "12:00", calendarType: "SOLAR", leapMonth: false });

export default function TeamCompatibilityPage() {
  const [members, setMembers] = useState<Member[]>([newMember(1, "나"), newMember(2)]);
  const [result, setResult] = useState<TeamResult>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!hasToken()) return;
    api<Profile>("/api/profile", {}, true).then(profile => {
      setMembers(current => current.map(member => member.id === 1 ? {
        ...member,
        name: profile.displayName || "나",
        birthDate: profile.birthDate,
        birthTime: profile.birthTime.slice(0, 5),
        calendarType: profile.calendarType,
        leapMonth: profile.leapMonth,
      } : member));
    }).catch(() => {});
  }, []);

  function update(id: number, patch: Partial<Member>) {
    setMembers(current => current.map(member => member.id === id ? { ...member, ...patch } : member));
    setResult(undefined); setError("");
  }

  function addMember() { if (members.length < 5) setMembers(current => [...current, newMember(nextId++)]); }
  function removeMember(id: number) { if (members.length > 2) setMembers(current => current.filter(member => member.id !== id)); setResult(undefined); }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (members.some(member => !member.name.trim() || !member.birthDate || !member.birthTime)) return;
    setBusy(true); setError("");
    try {
      const calculations = await Promise.all(members.map(member => api<Calculation>("/api/fortune/calculate", {
        method: "POST",
        body: JSON.stringify({ birthDate: member.birthDate, birthTime: member.birthTime.length === 5 ? `${member.birthTime}:00` : member.birthTime, calendarType: member.calendarType, leapMonth: member.leapMonth }),
      })));
      setResult(analyzeTeam(members.map(member => member.name.trim()), calculations));
      window.scrollTo({ top: 0, behavior: "smooth" });
    } catch (reason) { setError(reason instanceof Error ? reason.message : "팀플 궁합을 계산하지 못했습니다."); }
    finally { setBusy(false); }
  }

  return <main className="app-shell content-shell team-shell"><Header title="팀플 궁합" /><section className="team-content">
    {!result ? <><header className="team-intro"><img src="/images/mascot-major-clipboard.png" alt="팀원의 사주를 살펴보는 여우 마스코트" /><h1>우리 팀, 호흡이 잘 맞을까?</h1></header>
      <form className="team-form" onSubmit={submit}><div className="team-member-list">{members.map((member, index) => <fieldset className="team-member" key={member.id}>{members.length > 2 && index > 0 && <button className="team-remove" type="button" onClick={() => removeMember(member.id)} aria-label={`${member.name || `팀원 ${index + 1}`} 삭제`}>−</button>}<input aria-label={`${index + 1}번째 팀원 이름`} value={member.name} onChange={event => update(member.id, { name: event.target.value })} placeholder="팀원 이름" maxLength={12} /><div className="team-birth-row"><div className="team-picker-field team-date-field"><BirthDateField ariaLabel={`${member.name || `팀원 ${index + 1}`} 생년월일`} value={member.birthDate} calendar={member.calendarType} leap={member.leapMonth} onChange={(birthDate, calendarType, leapMonth) => update(member.id, { birthDate, calendarType, leapMonth })} /></div><div className="team-picker-field team-time-field"><BirthTimeField ariaLabel={`${member.name || `팀원 ${index + 1}`} 태어난 시간`} value={member.birthTime} onChange={birthTime => update(member.id, { birthTime })} /></div></div></fieldset>)}</div>
        {members.length < 5 && <button className="team-add" type="button" onClick={addMember}>＋ 한 명 더 기록하기</button>}{error && <p className="team-error" role="alert">{error}</p>}<button className="primary team-submit" disabled={busy || members.some(member => !member.name.trim())}>{busy ? "사주를 맞춰보는 중…" : `${members.length}명 팀플 궁합 보기`}</button><small className="team-form-note">입력한 정보는 궁합 계산에만 사용되며 저장되지 않아요.</small></form></> : <TeamResultView result={result} members={members} onReset={() => setResult(undefined)} />}
  </section></main>;
}

function TeamResultView({ result, members, onReset }: { result: TeamResult; members: Member[]; onReset: () => void }) {
  return <section className="team-result">
    <header className="team-result-summary">
      <p className="team-pair">{members.map(member => member.name.trim()).join(" · ")}</p>
      <div className="team-score"><strong>{result.score}</strong><span>점</span></div>
      <h1>{result.title}</h1>
    </header>
    <div className="team-result-divider" aria-hidden="true"><i>✦</i></div>
    <section className="team-result-advice"><h2>팀 전체 조언</h2><p>{result.tip}</p></section>
    <section className="team-roles"><h2>추천 역할</h2>{result.roles.map(item => <div key={item.name}><b>{item.name}</b><em>{item.role}</em></div>)}</section>
    <div className="team-result-divider" aria-hidden="true"><i>✦</i></div>
    <section className="team-pairs"><h2>팀원별 궁합</h2>{result.pairs.map(pair => <article key={`${pair.first}-${pair.second}`}><div><b>{pair.first}<i>×</i>{pair.second}</b><strong>{pair.score}<small>점</small></strong></div><p>{pair.summary}</p></article>)}</section>
    <button className="team-reset" type="button" onClick={onReset}>팀원 정보 다시 입력하기</button>
    <small className="team-disclaimer">전통 명리의 오행 관계와 지지 합충을 바탕으로 보는 참고용 결과예요.</small>
  </section>;
}
