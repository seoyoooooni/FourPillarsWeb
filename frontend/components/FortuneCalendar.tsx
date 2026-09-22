"use client";

import Image from "next/image";
import { useEffect, useMemo, useState } from "react";
import { api } from "@/lib/api";

type CheckedDay = { date:string; score:number; annualRank:number; wealth:number; love:number; health:number; career:number; relationships:number; study:number };
type CalendarResponse = { year:number; month:number; checkedDays:CheckedDay[] };

const weekday = ["일","월","화","수","목","금","토"];
const todayKey = localKey(new Date());
const calendarBookArt = {
  3: { src:"/images/mascot-calendar-book-3weeks-768.png", width:768, height:809 },
  4: { src:"/images/mascot-calendar-book-4weeks-v2-768.png", width:768, height:831 },
  5: { src:"/images/mascot-calendar-book-5weeks-anchored-768.png", width:768, height:904 },
  6: { src:"/images/mascot-calendar-book-6weeks-v2-768.png", width:768, height:919 },
} as const;

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
  const usedCellCount = firstDay + count;
  const weekCount = Math.max(3,Math.min(6,Math.ceil(usedCellCount/7))) as keyof typeof calendarBookArt;
  const cells = Array.from({length:weekCount*7},(_,index)=>index<firstDay||index>=usedCellCount?null:index-firstDay+1);
  const bookArt = calendarBookArt[weekCount];
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

  return <section className="fortune-calendar-shell" aria-label="오늘의 운세 확인 기록" onPointerDown={event=>event.stopPropagation()}>
    <div className={`calendar-book weeks-${weekCount}`} style={{aspectRatio:`${bookArt.width}/${bookArt.height}`}}>
      <Image className="calendar-book-art" src={bookArt.src} alt={`${weekCount}주 달력책을 잡고 있는 별여우`} width={bookArt.width} height={bookArt.height} priority />
      <div className="fortune-calendar">
        <header><button disabled={!canPrevious} onClick={()=>move(-1)} aria-label="이전 달">‹</button><strong>{cursor.year}. {String(cursor.month).padStart(2,"0")}</strong><button disabled={!canNext} onClick={()=>move(1)} aria-label="다음 달">›</button></header>
        <div className="calendar-weekdays">{weekday.map(day=><span key={day}>{day}</span>)}</div>
        <div className={`calendar-grid${loading?" is-loading":""}`}>{cells.map((day,index)=>{
          if(day===null)return <i key={`blank-${index}`}/>;
          const key=`${cursor.year}-${String(cursor.month).padStart(2,"0")}-${String(day).padStart(2,"0")}`;
          const checked=byDate.has(key);
          return <button key={key} className={`${checked?"checked ":""}${selected===key?"selected ":""}${todayKey===key?"today":""}`} onClick={()=>setSelected(key)} aria-label={`${cursor.month}월 ${day}일${checked?", 운세 확인함":""}`}><span>{day}</span>{checked&&<b/>}</button>
        })}</div>
      </div>
    </div>
    <div className={`calendar-summary-reveal${selected?" is-open":""}`}><div>
      {selected&&<div className={`calendar-summary${selectedDay?" has-chart":""}`}>{selectedDay?<><header className="calendar-summary-heading"><strong>{Number(selectedDay.date.slice(5,7))}월 {Number(selectedDay.date.slice(8))}일</strong><div><b>{selectedDay.score}<small>점</small></b><span>연간 {selectedDay.annualRank}위</span></div></header><MiniRadar day={selectedDay}/></>:<p>{error?"기록을 불러오지 못했어요. 잠시 후 다시 열어 주세요.":"이날 확인한 운세가 없어요."}</p>}</div>}
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
