"use client";
import Link from "next/link";
import { useEffect, useState } from "react"; import { Header } from "@/components/Header"; import { api, Calculation, hasToken, Profile } from "@/lib/api";
import { explanationFor } from "@/lib/saju-explanations";
const names: Record<string, string> = { year: "연", month: "월", day: "일", hour: "시", year_gan: "연간", month_gan: "월간", day_gan: "일간", hour_gan: "시간", year_ji: "연지", month_ji: "월지", day_ji: "일지", hour_ji: "시지" };
const textMap = (v: Record<string, string>) => Object.entries(v).map(([k, value]) => `${names[k] ?? k} ${value}`).join(" · ");
export default function SajuPage() {
  const [result, setResult] = useState<Calculation>(); const [error, setError] = useState("");
  useEffect(()=>{if(!hasToken()){setError("로그인이 필요합니다.");return}api<Profile>("/api/profile",{},true).then(profile=>api<Calculation>("/api/fortune/calculate",{method:"POST",body:JSON.stringify({birthDate:profile.birthDate,birthTime:profile.birthTime,calendarType:profile.calendarType,leapMonth:profile.leapMonth,gender:profile.gender})})).then(setResult).catch(x=>setError(x instanceof Error?x.message:"계산에 실패했습니다."))},[]);
  return <main className={`app-shell wide ${result ? "result-shell" : "form-shell"}`}><Header title="평생 사주" />{error ? <section className="empty-state"><p>{error}</p><Link className="primary link-button" href="/profile">프로필 설정하기</Link></section> : !result ? <div className="loading">프로필 사주를 계산하는 중…</div> : <Results result={result} />}</main>;
}
function Results({ result }: { result: Calculation }) {
  const sections = [
    ["십성과 지장간", [["십성", textMap(result.analysis.tenGods)], ["지장간", Object.entries(result.analysis.hiddenStems).map(([k, v]) => `${names[k]} ${v.map(x => `${x.stem}(${x.position}·${x.tenGod})`).join(", ")}`).join(" · ")]]],
    ["기본 분석", [["십이운성", textMap(result.stageTwoAnalysis.twelveStages)], ["공망", result.stageTwoAnalysis.gongmang.join(" · ")], ["십이신살 · 연지 기준", textMap(result.stageTwoAnalysis.twelveSinsalByYearBranch)], ["십이신살 · 일지 기준", textMap(result.stageTwoAnalysis.twelveSinsalByDayBranch)]]],
    ["용신 분석", [["용신 오행", result.yongsinAnalysis.yongsinElement], ["일간 강약", `${result.yongsinAnalysis.strength} (${result.yongsinAnalysis.strengthScore})`], ["십성 범주", Object.entries(result.yongsinAnalysis.categoryCounts).map(([k,v]) => `${k} ${v}`).join(" · ")], ["신약 우선 기준", result.yongsinAnalysis.weakPriority], ["신강 우선 기준", result.yongsinAnalysis.strongPriority], ["판정 근거", result.yongsinAnalysis.yongsinReason]]],
    ["신살 분석", [["천덕 대응값", result.stageThreeAnalysis.cheondeokValue], ["길신", hits(result.stageThreeAnalysis.gilsinByPillar)], ["주의해서 볼 신살", hits(result.stageThreeAnalysis.hyungsalByPillar)], ["원진살", result.stageThreeAnalysis.wonjinPairs.length ? result.stageThreeAnalysis.wonjinPairs.map(p => `${names[p.first]}-${names[p.second]}`).join(" · ") : "없음"]]],
    ...(result.daeunAnalysis ? [["대운", [
      ["대운 방향", `${result.daeunAnalysis.forward ? "순행" : "역행"} · ${result.daeunAnalysis.startAge}세부터 10년 단위로 바뀌어요`],
      ["대운 목록", result.daeunAnalysis.periods.map(p => `${p.startAge}세 ${p.stem}${p.branch}(${p.stemTenGod}·${p.branchTenGod})`).join(" · ")],
    ]] as [string, [string, string][]]] : []),
  ] as [string, [string, string][]][];
  return <section className="results"><p className="result-guide">ⓘ 각 사주 결과를 누르면 쉬운 설명이 펼쳐집니다.</p><PillarSection pillars={result.pillars} /><DayMasterSection value={result.analysis.dayMaster} /><ElementSection counts={result.analysis.elementCounts} />{sections.map(([title, rows]) => <article key={title}><h2>{title}</h2>{rows.map(([label,value]) => <details key={label}><summary>{label}<span>{value}</span></summary><p>{explanationFor(label,result)}</p></details>)}</article>)}</section>;
}

const pillarOrder = ["year", "month", "day", "hour"] as const;
const pillarLabels = { year: "연주", month: "월주", day: "일주", hour: "시주" };
const pillarExplanations = {
  year: "태어난 해의 기둥으로, 집안의 배경과 어린 시절처럼 삶의 바깥 기반을 살펴보는 정보입니다.",
  month: "태어난 달의 기둥으로, 성장 환경과 사회생활에서 드러나는 성향을 살펴보는 정보입니다.",
  day: "태어난 날의 기둥으로, 나 자신을 나타내는 일간과 가까운 관계의 흐름을 함께 살펴봅니다.",
  hour: "태어난 시간의 기둥으로, 내면의 생각과 이후의 삶, 결과를 만들어가는 방식을 살펴봅니다.",
};

function PillarSection({ pillars }: { pillars: Record<string, string> }) {
  const [open, setOpen] = useState<keyof typeof pillarExplanations>();
  const toggle = (key: keyof typeof pillarExplanations) => setOpen(current => current === key ? undefined : key);

  return <article className="pillar-section"><h2>사주 원국</h2><div className="pillar-table"><span aria-hidden="true" />{pillarOrder.map(key => <button type="button" className={key === "day" ? "is-day" : ""} onClick={() => toggle(key)} key={`${key}-label`}>{pillarLabels[key]}</button>)}<span className="pillar-row-label">천간</span>{pillarOrder.map(key => <button type="button" className={`pillar-value ${key === "day" ? "is-day" : ""}`} onClick={() => toggle(key)} key={`${key}-stem`}>{Array.from(pillars[key] ?? "")[0]}</button>)}<span className="pillar-row-label">지지</span>{pillarOrder.map(key => <button type="button" className={`pillar-value ${key === "day" ? "is-day" : ""}`} onClick={() => toggle(key)} key={`${key}-branch`}>{Array.from(pillars[key] ?? "")[1]}</button>)}</div>{open && <p className="result-explanation">{pillarExplanations[open]}</p>}</article>;
}

function DayMasterSection({ value }: { value: string }) {
  const [open, setOpen] = useState(false);
  return <article className="day-master-section"><h2>나를 나타내는 일간</h2><button type="button" onClick={() => setOpen(current => !current)}>{value}</button>{open && <p className="result-explanation">{value}은 사주에서 나 자신을 나타내는 기준점입니다. 다른 글자와 오행의 관계도 이 일간을 중심으로 해석합니다.</p>}</article>;
}

function ElementSection({ counts }: { counts: Record<string, number> }) {
  const [open, setOpen] = useState(false);
  const order = ["목", "화", "토", "금", "수"];
  const maximum = Math.max(1, ...order.map(key => counts[key] ?? 0));
  return <article className="element-section"><h2>오행 분포</h2>{order.map(key => { const count = counts[key] ?? 0; return <button type="button" className="element" onClick={() => setOpen(current => !current)} key={key}><span>{key}</span><i><b className={count === maximum ? "is-maximum" : ""} style={{ width: `${count / maximum * 100}%` }} /></i><em>{count}</em></button>; })}{open && <p className="result-explanation">목·화·토·금·수의 개수를 비교한 값입니다. 개수만으로 좋고 나쁨을 정하지 않고 계절과 일간의 힘을 함께 봅니다.</p>}</article>;
}
function hits(v: Record<string, string[]>) { return Object.keys(v).length ? Object.entries(v).map(([k,a]) => `${names[k]} ${a.join(", ")}`).join(" · ") : "없음"; }
