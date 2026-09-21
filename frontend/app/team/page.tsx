"use client";

import { FormEvent, useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { BirthDateField, BirthTimeField } from "@/components/BirthPickers";
import { api, Calculation, hasToken, Profile } from "@/lib/api";

type Member = { id: number; name: string; birthDate: string; birthTime: string; calendarType: "SOLAR" | "LUNAR"; leapMonth: boolean };
type PairResult = { first: string; second: string; score: number; summary: string };
type TeamResult = { score: number; title: string; pairs: PairResult[]; roles: { name: string; role: string }[]; tip: string };

const STEM_ELEMENT: Record<string, string> = { 갑: "목", 을: "목", 병: "화", 정: "화", 무: "토", 기: "토", 경: "금", 신: "금", 임: "수", 계: "수" };
const GENERATES: Record<string, string> = { 목: "화", 화: "토", 토: "금", 금: "수", 수: "목" };
const CONTROLS: Record<string, string> = { 목: "토", 토: "수", 수: "화", 화: "금", 금: "목" };
const BRANCH_COMBINATIONS = new Set(["자축", "인해", "묘술", "진유", "사신", "오미"]);
const BRANCH_CLASHES = new Set(["자오", "축미", "인신", "묘유", "진술", "사해"]);
const BRANCH_HARMS = new Set(["자미", "축오", "인사", "묘진", "신해", "유술"]);
const ROLE_BY_ELEMENT: Record<string, string> = { 목: "기획·방향 설정", 화: "발표·분위기 전환", 토: "일정·의견 조율", 금: "자료 정리·최종 검수", 수: "조사·아이디어 확장" };

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
      setResult(analyzeTeam(members, calculations));
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

function analyzeTeam(members: Member[], calculations: Calculation[]): TeamResult {
  const pairs: PairResult[] = [];
  for (let first = 0; first < members.length; first++) for (let second = first + 1; second < members.length; second++) pairs.push(analyzePair(members[first].name.trim(), calculations[first], members[second].name.trim(), calculations[second]));
  const score = Math.round(pairs.reduce((sum, pair) => sum + pair.score, 0) / pairs.length);
  const roles = calculations.map((calculation, index) => ({ name: members[index].name.trim(), role: roleFor(calculation) }));
  const title = score >= 90 ? "서로의 빈틈을 채우는 좋은 팀" : score >= 80 ? "역할을 나누면 강해지는 팀" : score >= 70 ? "규칙을 정하면 편안한 팀" : "속도를 맞추는 연습이 필요한 팀";
  const tip = score >= 85 ? "서로 다른 강점을 믿고 역할을 분명히 나누면 완성도와 속도를 함께 잡을 수 있어요." : score >= 72 ? "회의 시작 전에 결정 방식과 마감 기준을 정하면 의견 차이가 팀의 장점으로 바뀔 수 있어요." : "중간 점검 시간을 짧게 자주 만들고, 중요한 결정은 말보다 문서로 남기는 편이 좋아요.";
  return { score, title, pairs: pairs.sort((a, b) => b.score - a.score), roles, tip };
}

function analyzePair(firstName: string, first: Calculation, secondName: string, second: Calculation): PairResult {
  const firstDay = Array.from(first.pillars.day ?? ""); const secondDay = Array.from(second.pillars.day ?? "");
  const firstElement = STEM_ELEMENT[firstDay[0]]; const secondElement = STEM_ELEMENT[secondDay[0]];
  let score = 72; const reasons: string[] = [];
  if (firstElement === secondElement) { score += 5; reasons.push("기본적으로 일하는 결이 비슷해요"); }
  else if (GENERATES[firstElement] === secondElement || GENERATES[secondElement] === firstElement) { score += 12; reasons.push("서로의 오행을 자연스럽게 북돋는 관계예요"); }
  else if (CONTROLS[firstElement] === secondElement || CONTROLS[secondElement] === firstElement) { score -= 4; reasons.push("속도와 판단 기준이 다를 수 있어요"); }
  if (hasBranchRelation(BRANCH_COMBINATIONS, firstDay[1], secondDay[1])) { score += 12; reasons.push("일지의 합이 있어 호흡을 맞추기 좋아요"); }
  if (hasBranchRelation(BRANCH_CLASHES, firstDay[1], secondDay[1])) { score -= 12; reasons.push("의견이 정면으로 부딪힐 때가 있어요"); }
  else if (hasBranchRelation(BRANCH_HARMS, firstDay[1], secondDay[1])) { score -= 6; reasons.push("작은 오해는 바로 확인하는 편이 좋아요"); }
  if (first.yongsinAnalysis.yongsinElement === secondElement || second.yongsinAnalysis.yongsinElement === firstElement) { score += 7; reasons.push("한 사람의 부족한 기운을 다른 사람이 보완해요"); }
  const balance = elementComplement(first.analysis.elementCounts, second.analysis.elementCounts); score += balance;
  if (balance >= 5) reasons.push("두 사람의 오행 분포가 서로의 빈틈을 채워줘요");
  score = Math.max(45, Math.min(98, score));
  return { first: firstName, second: secondName, score, summary: `${(reasons.length ? reasons : ["큰 충돌 없이 무난하게 역할을 맞출 수 있어요"]).slice(0, 2).join(". ")}.` };
}

function elementComplement(first: Record<string, number>, second: Record<string, number>) {
  let complement = 0;
  for (const element of ["목", "화", "토", "금", "수"]) if (((first[element] ?? 0) <= 1 && (second[element] ?? 0) >= 2) || ((second[element] ?? 0) <= 1 && (first[element] ?? 0) >= 2)) complement++;
  return Math.min(7, complement * 2);
}
function roleFor(calculation: Calculation) { const strongest = Object.entries(calculation.analysis.elementCounts).sort((a, b) => b[1] - a[1])[0]?.[0] ?? "토"; return ROLE_BY_ELEMENT[strongest] ?? ROLE_BY_ELEMENT.토; }
function hasBranchRelation(relations: Set<string>, first?: string, second?: string) {
  if (!first || !second) return false;
  return relations.has(first + second) || relations.has(second + first);
}
