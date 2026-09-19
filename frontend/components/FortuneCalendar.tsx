"use client";

import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";
import Image from "next/image";
import { HandDrawnBorder } from "@/components/HandDrawnBorder";

type CheckedDay = { date:string; score:number; annualRank:number; wealth:number; love:number; health:number; career:number; relationships:number; study:number };
type CalendarResponse = { year:number; month:number; checkedDays:CheckedDay[] };

const weekday = ["일","월","화","수","목","금","토"];
const todayKey = localKey(new Date());

export function FortuneCalendar() {
  const now = new Date();
  const [cursor,setCursor] = useState({year:now.getFullYear(),month:now.getMonth()+1});
  const [days,setDays] = useState<CheckedDay[]>([]);
  const [selected,setSelected] = useState<string>();
  const [loading,setLoading] = useState(false);
  const [error,setError] = useState(false);
  const byDate = useMemo(()=>new Map(days.map(day=>[day.date,day])),[days]);
  const firstDay = new Date(cursor.year,cursor.month-1,1).getDay();
  const count = new Date(cursor.year,cursor.month,0).getDate();
  const cells = Array.from({length:firstDay+count},(_,index)=>index<firstDay?null:index-firstDay+1);
  const selectedDay = selected ? byDate.get(selected) : undefined;
  const canPrevious = cursor.year > 1926 || cursor.month > 1;
  const canNext = cursor.year < now.getFullYear() || cursor.month < now.getMonth()+1;

  useEffect(()=>{
    let active=true; setLoading(true); setError(false);
    api<CalendarResponse>(`/api/fortune/calendar?year=${cursor.year}&month=${cursor.month}`,{},true)
      .then(result=>{if(!active)return;setDays(result.checkedDays);setSelected(undefined)})
      .catch(()=>{if(active){setDays([]);setSelected(undefined);setError(true)}})
      .finally(()=>{if(active)setLoading(false)});
    return()=>{active=false};
  },[cursor]);

  function move(delta:number){setSelected(undefined);setCursor(value=>{const date=new Date(value.year,value.month-1+delta,1);return{year:date.getFullYear(),month:date.getMonth()+1}})}

  return <section className="fortune-calendar" aria-label="오늘의 운세 확인 기록" onPointerDown={event=>event.stopPropagation()}>
    <HandDrawnBorder className="calendar-hand-border">
      <path d="M6 0 C20 0 34 0 49 0 C65 0 79 0 93.5 0 C96 1 97 3.8 96.6 8 C97.2 27 96 43 96.5 60 C96.9 74 95.8 87 96.2 93.5 C96.3 96.2 94.9 97.4 92.2 97.2 C77 96.6 65 98 50 97.3 C35 98 22 96.8 7.2 97.4 C4.6 97.5 3.4 95.7 3.6 92.8 C3 77 4.2 62 3.6 47 C3.2 32 4.2 19 3.5 7.7 C3.2 3.8 4.1 1 6 0 Z"/>
      <path className="calendar-hand-top-line" d="M6 0 C20 0 34 0 49 0 C65 0 79 0 93.5 0"/>
    </HandDrawnBorder>
    <div className="calendar-mascot" aria-hidden="true">
      <Image src="/images/mascot-calendar-holder-watercolor.png" alt="" width={1536} height={1024}/>
    </div>
    <header><button disabled={!canPrevious} onClick={()=>move(-1)} aria-label="이전 달">‹</button><strong>{cursor.year}. {String(cursor.month).padStart(2,"0")}</strong><button disabled={!canNext} onClick={()=>move(1)} aria-label="다음 달">›</button></header>
    <div className="calendar-weekdays">{weekday.map(day=><span key={day}>{day}</span>)}</div>
    <div className={`calendar-grid${loading?" is-loading":""}`}>{cells.map((day,index)=>{
      if(day===null)return <i key={`blank-${index}`}/>;
      const key=`${cursor.year}-${String(cursor.month).padStart(2,"0")}-${String(day).padStart(2,"0")}`;
      const checked=byDate.has(key);
      return <button key={key} className={`${checked?"checked ":""}${selected===key?"selected ":""}${todayKey===key?"today":""}`} onClick={()=>setSelected(key)} aria-label={`${cursor.month}월 ${day}일${checked?", 운세 확인함":""}`}><span>{day}</span>{checked&&<b/>}</button>
    })}</div>
    <div className={`calendar-summary-reveal${selected?" is-open":""}`}><div>
      {selected&&<div className={`calendar-summary${selectedDay?" has-chart":""}`}>{selectedDay?<><div><span>{Number(selectedDay.date.slice(5,7))}월 {Number(selectedDay.date.slice(8))}일</span><strong>{selectedDay.score}<small>점</small><em> · {selectedDay.annualRank}위</em></strong></div><MiniRadar day={selectedDay}/></>:<p>{error?"기록을 불러오지 못했어요. 잠시 후 다시 열어 주세요.":"이날 확인한 운세가 없어요."}</p>}</div>}
    </div></div>
  </section>;
}

const radarFields: [keyof CheckedDay,string][] = [["wealth","재물"],["love","애정"],["health","건강"],["career","직업"],["relationships","관계"],["study","학업"]];
function MiniRadar({day}:{day:CheckedDay}) {
  const values=radarFields.map(([key])=>day[key] as number);
  const point=(radius:number,index:number)=>`${90+radius*Math.cos(-Math.PI/2+index*Math.PI/3)},${76+radius*Math.sin(-Math.PI/2+index*Math.PI/3)}`;
  return <svg className="calendar-radar" viewBox="0 0 180 155" role="img" aria-label="선택한 날의 분야별 운세 그래프">
    {[28,54].map(radius=><polygon key={radius} points={radarFields.map((_,index)=>point(radius,index)).join(" ")} fill="none" stroke="var(--line)" strokeWidth=".8"/>)}
    <polygon points={values.map((value,index)=>point(54*value/100,index)).join(" ")} fill="rgba(14,40,100,.12)" stroke="#0e2864" strokeWidth="1.5"/>
    {radarFields.map(([_,label],index)=>{const [x,y]=point(68,index).split(",");return <text key={label} x={x} y={y} textAnchor="middle" dominantBaseline="middle">{label} {values[index]}</text>})}
  </svg>;
}

function localKey(date:Date){return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,"0")}-${String(date.getDate()).padStart(2,"0")}`}
