"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { PointerEvent, useEffect, useRef, useState } from "react";
import { api, hasToken, logout, Profile } from "@/lib/api";
import { SignupOnboarding } from "@/components/SignupOnboarding";
import { FortuneCalendar } from "@/components/FortuneCalendar";

const cards = [
  { href: "/decision", image: "/images/card-compatibility-inha-v2.png", title: "운명저울", number: "I" },
  { href: "/major", image: "/images/card-major-inha-v2.png", title: "학과 추천", number: "II" },
  { href: "/today", image: "/images/card-airplane.png", title: "오늘의 운세", number: "III" },
  { href: "/saju", image: "/images/card-lifetime-inha-v2.png", title: "평생 사주", number: "IV" },
  { href: "/team", image: "/images/card-campus-inha-v2.png", title: "팀플 궁합", number: "V" },
];
const angles = [-11, -5, 0, 5, 11];
const offsets = [
  ["clamp(-360px, -31cqw, -230px)", 88],
  ["clamp(-235px, -20cqw, -145px)", 46],
  ["0px", 0],
  ["clamp(145px, 20cqw, 235px)", 46],
  ["clamp(230px, 31cqw, 360px)", 88],
] as const;
const scales = [.72,.84,1,.84,.72];
const alphas = [.2,.38,1,.38,.2];
let rememberedFocus = 2;

export default function Home() {
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [focus, setFocus] = useState(() => rememberedFocus); const [menu, setMenu] = useState(false); const [calendarOpen,setCalendarOpen]=useState(false); const [profile,setProfile]=useState<Profile>(); const [drawerX,setDrawerX]=useState(0); const [drawerDragging,setDrawerDragging]=useState(false);
  const startX = useRef(0); const didSwipe=useRef(false); const drawerStartX=useRef<number|null>(null); const drawerPointerId=useRef<number|null>(null); const closeTimer=useRef<number|null>(null); const loggedIn = hasToken();
  function cardClick(index:number) { if(didSwipe.current){didSwipe.current=false;return} if(cards[index].href !== "#") router.push(cards[index].href); }
  function pointerDown(e:PointerEvent<HTMLDivElement>) { startX.current=e.clientX;didSwipe.current=false; }
  function pointerUp(e:PointerEvent<HTMLDivElement>) { const delta=e.clientX-startX.current; if(Math.abs(delta)>=32){didSwipe.current=true;setFocus(v=>Math.max(0,Math.min(cards.length-1,v+(delta<0?1:-1))));} }
  function pointerCancel(){didSwipe.current=false}
  function clearCloseTimer(){if(closeTimer.current!==null){window.clearTimeout(closeTimer.current);closeTimer.current=null}}
  function openMenu(){clearCloseTimer();setCalendarOpen(false);setMenu(true);if(loggedIn)api<Profile>("/api/profile",{},true).then(setProfile).catch(()=>setProfile(undefined))}
  function openCalendar(){setMenu(false);setCalendarOpen(true)}
  function closeMenu(){clearCloseTimer();setDrawerDragging(false);setDrawerX(-286);closeTimer.current=window.setTimeout(()=>{setMenu(false);setDrawerX(0);closeTimer.current=null},180)}
  function drawerDown(e:PointerEvent<HTMLElement>){clearCloseTimer();drawerPointerId.current=e.pointerId;e.currentTarget.setPointerCapture(e.pointerId);drawerStartX.current=e.clientX-drawerX;setDrawerDragging(true)}
  function drawerMove(e:PointerEvent<HTMLElement>){if(drawerStartX.current===null||e.pointerId!==drawerPointerId.current)return;setDrawerX(Math.max(-286,Math.min(0,e.clientX-drawerStartX.current)))}
  function drawerUp(e:PointerEvent<HTMLElement>){if(e.pointerId!==drawerPointerId.current)return;drawerPointerId.current=null;drawerStartX.current=null;setDrawerDragging(false);if(drawerX<=-71.5)closeMenu();else setDrawerX(0)}
  useEffect(() => setMounted(true), []);
  useEffect(() => { rememberedFocus = focus; }, [focus]);
  if (!mounted) return <main className="app-shell auth-shell" />;
  if (!loggedIn) return <main className="app-shell auth-shell"><SignupOnboarding /></main>;
  return <main className="maui-home">
    <button className="maui-menu-button" onClick={openMenu} aria-label="메뉴">☰</button>
    <button className="maui-calendar-button" onClick={openCalendar} aria-label="운세 캘린더 열기">
      <svg viewBox="0 0 32 32" aria-hidden="true"><rect x="4.5" y="7.5" width="23" height="20" rx="4"/><path d="M10 4.5v6M22 4.5v6M4.5 13h23M10 18h2M15 18h2M20 18h2M10 23h2M15 23h2M20 23h2"/></svg>
    </button>
    <svg className="brand-logo" viewBox="0 0 88 88" role="img" aria-label="별과 펼친 책 로고">
      <path d="M44 8 46 29 58 20 49 32 66 32 49 35 58 47 46 38 44 59 42 38 30 47 39 35 22 32 39 32 30 20 42 29Z" fill="currentColor"/>
      <path d="M12 45h2m60 0h2" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <path d="M20 65c9-1.5 17-.4 24 3 7-3.4 15-4.5 24-3M20 70c9-1.5 17-.4 24 3 7-3.4 15-4.5 24-3M20 75c9-1.5 17-.4 24 3 7-3.4 15-4.5 24-3M44 68v10" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
    <div className="maui-deck" onPointerDown={pointerDown} onPointerUp={pointerUp} onPointerCancel={pointerCancel}>
      {cards.map((card,index)=>{const slot=index-focus; const visible=slot>=-2&&slot<=2; const pos=visible?slot+2:slot<0?0:4; return <button className={`maui-card${index===focus?" selected":""}`} key={card.title} onClick={()=>cardClick(index)} style={{transform:`translate(calc(-50% + ${offsets[pos][0]}), ${offsets[pos][1]}px) rotate(${angles[pos]}deg) scale(${scales[pos]})`,opacity:visible?alphas[pos]:0,zIndex:10-Math.abs(slot)}}><Image className="card-fox-topper" src="/images/card-fox-topper-640.png" width={640} height={474} alt="" priority draggable={false}/><div className="card-inner"><span className="card-number">{card.number}</span><div className="card-image"><Image src={card.image} fill sizes="(max-width: 480px) 64vw, 292px" alt="" priority draggable={false} onDragStart={(event)=>event.preventDefault()} /></div><strong>{card.title}</strong></div></button>})}
    </div>
    <div className="page-dots">{cards.map((_,i)=><i className={i===focus?"active":""} key={i}/>)}</div>
    {calendarOpen&&<div className="calendar-popover-dim" onClick={()=>setCalendarOpen(false)}><section className="calendar-popover" role="dialog" aria-modal="true" aria-label="운세 캘린더" onClick={e=>e.stopPropagation()}><button className="calendar-popover-close" onClick={()=>setCalendarOpen(false)} aria-label="운세 캘린더 닫기"><svg viewBox="0 0 20 20" aria-hidden="true"><path d="M5 5l10 10M15 5 5 15"/></svg></button><FortuneCalendar/></section></div>}
    {menu&&<div className="maui-dim" onClick={closeMenu}><aside className="maui-drawer" style={{transform:`translateX(${drawerX}px)`,transition:drawerDragging?"none":"transform 180ms cubic-bezier(.55,0,1,.45)"}} onPointerDown={drawerDown} onPointerMove={drawerMove} onPointerUp={drawerUp} onPointerCancel={drawerUp} onClick={e=>e.stopPropagation()}><button className="drawer-profile" onPointerDown={e=>e.stopPropagation()} onClick={()=>router.push(loggedIn?"/profile":"/login")}><strong>{loggedIn?(profile?.displayName??"프로필을 설정하세요"):"로그인하세요"}</strong>{profile&&<small>{formatBirthDate(profile.birthDate)}</small>}</button><hr/><button onPointerDown={e=>e.stopPropagation()} onClick={()=>{if(loggedIn)logout();else router.push("/login");closeMenu()}}>{loggedIn?"로그아웃":"로그인"}</button></aside></div>}
  </main>;
}
function formatBirthDate(value:string){const [y,m,d]=value.split("-").map(Number);return `${y}년 ${m}월 ${d}일`}
