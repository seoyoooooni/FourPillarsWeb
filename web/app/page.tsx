"use client";

import Image from "next/image";
import { useRouter } from "next/navigation";
import { PointerEvent, useRef, useState } from "react";
import { api, hasToken, logout, Profile } from "@/lib/api";

const cards = [
  { href: "#", image: "/images/card-compatibility.png", title: "궁합", number: "I" },
  { href: "#", image: "/images/card-test.png", title: "테스트", number: "II" },
  { href: "/today", image: "/images/card-today.png", title: "오늘의 운세", number: "III" },
  { href: "/saju", image: "/images/card-lifetime.png", title: "평생 사주", number: "IV" },
  { href: "#", image: "/images/card-newyear.png", title: "신년 운세", number: "V" },
];
const angles = [-11, -5, 0, 5, 11];
const offsets = [
  ["clamp(-360px, -31vw, -230px)", 88],
  ["clamp(-235px, -20vw, -145px)", 46],
  ["0px", 0],
  ["clamp(145px, 20vw, 235px)", 46],
  ["clamp(230px, 31vw, 360px)", 88],
] as const;
const scales = [.72,.84,1,.84,.72];
const alphas = [.2,.38,1,.38,.2];

export default function Home() {
  const router = useRouter();
  const [focus, setFocus] = useState(2); const [selected, setSelected] = useState<number>(); const [menu, setMenu] = useState(false); const [profile,setProfile]=useState<Profile>(); const [drawerX,setDrawerX]=useState(0); const [drawerDragging,setDrawerDragging]=useState(false);
  const startX = useRef(0); const drawerStartX=useRef<number|null>(null); const loggedIn = hasToken();
  function cardClick(index:number) { if(selected === index) { if(cards[index].href !== "#") router.push(cards[index].href); } else setSelected(index); }
  function pointerDown(e:PointerEvent) { startX.current=e.clientX; }
  function pointerUp(e:PointerEvent) { const delta=e.clientX-startX.current; if(Math.abs(delta)>=32){setSelected(undefined);setFocus(v=>Math.max(0,Math.min(4,v+(delta<0?1:-1))));} }
  function closeMenu(){setDrawerDragging(false);setDrawerX(-286);window.setTimeout(()=>{setMenu(false);setDrawerX(0)},180)}
  function drawerDown(e:PointerEvent<HTMLElement>){drawerStartX.current=e.clientX-drawerX;setDrawerDragging(true)}
  function drawerMove(e:PointerEvent<HTMLElement>){if(drawerStartX.current!==null)setDrawerX(Math.max(-286,Math.min(0,e.clientX-drawerStartX.current)))}
  function drawerUp(){drawerStartX.current=null;setDrawerDragging(false);if(drawerX<=-71.5)closeMenu();else setDrawerX(0)}
  return <main className="maui-home">
    <button className="maui-menu-button" onClick={()=>{setMenu(true);if(loggedIn)api<Profile>("/api/profile",{},true).then(setProfile).catch(()=>setProfile(undefined))}} aria-label="메뉴">☰</button>
    <div className="brand-logo" aria-label="Four Pillars"><i/><b/><b/><b/><b/><em/></div>
    <div className="maui-deck" onClick={()=>setSelected(undefined)} onPointerDown={pointerDown} onPointerUp={pointerUp}>
      {cards.map((card,index)=>{const slot=index-focus; const visible=slot>=-2&&slot<=2; const pos=visible?slot+2:slot<0?0:4; const chosen=selected===index; const x=chosen?"0px":offsets[pos][0]; return <button className={`maui-card ${chosen?"selected":""}`} key={card.title} onClick={e=>{e.stopPropagation();cardClick(index)}} style={{transform:`translate(calc(-50% + ${x}), ${chosen?-64:offsets[pos][1]}px) rotate(${chosen?0:angles[pos]}deg) scale(${chosen?1.14:scales[pos]})`,opacity:visible?(chosen?1:alphas[pos]):0,zIndex:chosen?20:10-Math.abs(slot)}}><div className="card-inner"><span className="card-number">{card.number}</span><div className="card-image"><Image src={card.image} fill sizes="(max-width: 480px) 72vw, 350px" alt="" priority /></div><strong>{card.title}</strong></div></button>})}
    </div>
    <div className="page-dots">{cards.map((_,i)=><i className={i===focus?"active":""} key={i}/>)}</div>
    {menu&&<div className="maui-dim" onClick={closeMenu}><aside className="maui-drawer" style={{transform:`translateX(${drawerX}px)`,transition:drawerDragging?"none":"transform 180ms cubic-bezier(.55,0,1,.45)"}} onPointerDown={drawerDown} onPointerMove={drawerMove} onPointerUp={drawerUp} onPointerCancel={drawerUp} onClick={e=>e.stopPropagation()}><button className="drawer-profile" onPointerDown={e=>e.stopPropagation()} onClick={()=>router.push(loggedIn?"/profile":"/login")}><strong>{loggedIn?(profile?.displayName??"프로필을 설정하세요"):"로그인하세요"}</strong>{profile&&<small>{formatBirthDate(profile.birthDate)}</small>}</button><hr/><div/><button onPointerDown={e=>e.stopPropagation()} onClick={()=>router.push("/settings")}>설정</button><button onPointerDown={e=>e.stopPropagation()} onClick={()=>{if(loggedIn)logout();else router.push("/login");closeMenu()}}>{loggedIn?"로그아웃":"로그인"}</button></aside></div>}
  </main>;
}
function formatBirthDate(value:string){const [y,m,d]=value.split("-").map(Number);return `${y}년 ${m}월 ${d}일`}
