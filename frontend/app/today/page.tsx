"use client";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Header } from "@/components/Header";
import { CampusFortuneCard } from "@/components/CampusFortuneCard";
import { api, hasToken } from "@/lib/api";
import { campusMessage, localDateKey } from "@/lib/campus-fortune";

type Today = { fortune: { overall:number; wealth:number; love:number; health:number; career:number; relationships:number; study:number; todayTenGod:string; energyDescription:string; interactions:{natalPosition:string;natalValue:string;todayValue:string;type:string}[] }; annualRank:{rank:number;totalDays:number;topPercent:number}; recentScores:{date:string;score:number;today:boolean}[] };
const fields: [keyof Today["fortune"], string][] = [["wealth","재물"],["love","애정"],["health","건강"],["career","직업"],["relationships","관계"],["study","학업"]];

export default function TodayPage() {
  const router=useRouter(); const [data,setData]=useState<Today>(); const [error,setError]=useState(""); const todayKey=localDateKey();
  useEffect(()=>{ if(!hasToken()){router.replace("/login");return} api<Today>("/api/fortune/today",{},true).then(setData).catch(e=>setError(e.message)); },[router]);
  return <main className="app-shell content-shell"><Header title="오늘의 운세" />{error ? <section className="empty-state"><p>{error}</p><Link className="primary link-button" href="/profile">설정하기</Link></section> : !data ? <div className="loading">오늘의 흐름을 살펴보는 중…</div> : <section className="today-result"><p className="result-meta">{new Intl.DateTimeFormat("ko-KR",{month:"long",day:"numeric"}).format(new Date())} · {data.annualRank.totalDays}일 중</p><div className="rank"><strong>{data.annualRank.rank}</strong><span>위</span></div><hr/><h2>오늘의 점수&nbsp;&nbsp;{data.fortune.overall}점</h2><Radar fortune={data.fortune}/><hr/><h2>최근 7일</h2><Weekly scores={data.recentScores}/><hr/><article><h2>오늘의 기운&nbsp;&nbsp;·&nbsp;&nbsp;{data.fortune.todayTenGod}</h2><p>{data.fortune.energyDescription}</p></article><hr/><article><h2>사주와 오늘</h2><p>{data.fortune.interactions.length ? data.fortune.interactions.map(x=>`${x.natalPosition} ${x.natalValue}↔${x.todayValue} ${x.type}`).join(" · ") : "오늘 일진과 원국 사이에 두드러진 합·충·형·파·해가 없습니다."}</p></article><hr/><article><h2>오늘의 캠퍼스 운세</h2><p>{campusMessage(data.fortune.overall,todayKey)}</p></article><CampusFortuneCard fortune={data.fortune} dateKey={todayKey}/></section>}</main>;
}

function Radar({fortune}:{fortune:Today["fortune"]}){const values=fields.map(([k])=>fortune[k] as number);const point=(r:number,i:number)=>`${140+r*Math.cos(-Math.PI/2+i*Math.PI/3)},${140+r*Math.sin(-Math.PI/2+i*Math.PI/3)}`;return <svg className="radar" viewBox="0 0 280 280"><polygon points={fields.map((_,i)=>point(92,i)).join(" ")} fill="none" stroke="var(--line)"/><polygon points={fields.map((_,i)=>point(46,i)).join(" ")} fill="none" stroke="var(--line)"/><polygon points={values.map((v,i)=>point(92*v/100,i)).join(" ")} fill="none" stroke="var(--ink)" strokeWidth="2.2"/>{fields.map(([_,label],i)=>{const [x,y]=point(120,i).split(",");return <text key={label} x={x} y={y} textAnchor="middle" dominantBaseline="middle">{label} {values[i]}</text>})}</svg>}
function Weekly({scores}:{scores:Today["recentScores"]}){const min=Math.max(0,Math.min(...scores.map(x=>x.score))-8),max=Math.min(100,Math.max(...scores.map(x=>x.score))+8),range=Math.max(1,max-min);const points=scores.map((x,i)=>`${16+i*(248/Math.max(1,scores.length-1))},${24+80*(max-x.score)/range}`);return <svg className="weekly" viewBox="0 0 280 122"><polyline points={points.join(" ")} fill="none" stroke="var(--line)" strokeWidth="2"/>{scores.map((x,i)=>x.today&&<g key={x.date}><circle cx={16+i*(248/Math.max(1,scores.length-1))} cy={24+80*(max-x.score)/range} r="5.5" fill="var(--ink)"/><text x={16+i*(248/Math.max(1,scores.length-1))} y={14+80*(max-x.score)/range} textAnchor="middle">{x.score}</text></g>)}</svg>}
