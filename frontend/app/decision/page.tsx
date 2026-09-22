"use client";
import Image from "next/image";
import {useState,type CSSProperties} from "react";
import {Header} from "@/components/Header";
import {tarotCardBack,tarotCards,TarotCard} from "@/lib/tarot-data";
export default function StudyFortunePage(){
 const[card,setCard]=useState<TarotCard|null>(null);const[drawing,setDrawing]=useState(false);const[selected,setSelected]=useState<number|null>(null);const[shuffling,setShuffling]=useState(false);const[shuffleSeed,setShuffleSeed]=useState(0);
 function draw(position:number){if(drawing||shuffling||card)return;setSelected(position);setDrawing(true);window.setTimeout(()=>{const saved=getSavedDailyCard();const n=crypto.getRandomValues(new Uint32Array(1))[0];const picked=saved??tarotCards[n%tarotCards.length];if(!saved)window.localStorage.setItem("daily-study-tarot",JSON.stringify({date:localDateKey(),cardId:picked.id}));setCard(picked);setDrawing(false)},700)}
 function shuffle(){if(drawing||shuffling)return;setShuffling(true);setShuffleSeed(value=>value+1);window.setTimeout(()=>setShuffling(false),750)}
 return <main className="app-shell content-shell study-tarot-shell"><Header title="학업운"/><section className="study-tarot-content" style={{paddingTop:18}}>
  {!card&&<header className="study-tarot-intro"><h1>뽑아보세요!</h1></header>}
  {!card&&<><div className={`tarot-fan${shuffling?" is-shuffling":""}`} style={{marginTop:52}} aria-label="섞여 있는 타로 카드">{Array.from({length:18},(_,i)=>{const turns=[-9,6,-3,8,-6,2,7,-5];const shifts=[-2,3,-1,2,0,2,-3,1];const order=i+shuffleSeed;const column=i%6;const row=Math.floor(i/6);return <button key={i} type="button" className={`tarot-fan-card${selected===i?" is-selected":""}`} style={{"--angle":`${turns[(order*5)%turns.length]}deg`,"--shift":`${shifts[(order*3)%shifts.length]}px`,"--mix-x":`${(2.5-column)*78}px`,"--mix-y":`${(1-row)*108}px`,"--shuffle-delay":`${(i%6)*18}ms`} as CSSProperties} onClick={()=>draw(i)} disabled={drawing||shuffling} aria-label={`${i+1}번째 타로 카드 선택`}><Image src={tarotCardBack} width={180} height={315} alt="" priority/></button>})}</div><button className="tarot-shuffle" type="button" onClick={shuffle} disabled={shuffling}>{shuffling?"섞는 중…":"카드 섞기"}</button></>}
  {card&&<div className="result-mascot-card"><div className="chosen-tarot"><Image src={card.image} width={240} height={420} alt={`${card.name} 카드`} priority/></div><Image className="result-fox" src="/images/mascot-tarot-holder.png" width={900} height={1200} alt="선택한 카드를 두 앞발로 잡고 있는 별여우" priority/></div>}
  <div className="study-tarot-live" aria-live="polite">{card&&<article className="study-tarot-result" style={{marginTop:0,paddingTop:6,borderTop:0}}>
   <p className="study-tarot-name-en">{card.englishName}</p><h2>{card.name}</h2>
   <div className="study-tarot-keywords">{card.keywords.map(x=><span key={x}>#{x}</span>)}</div>
   <div className="mascot-reading"><Image src="/images/mascot-guide-talking.png" width={150} height={150} alt="카드를 설명하는 여우 마스코트"/><div className="mascot-speech"><b>이 카드는 말이야…</b><p>{card.study.summary}</p></div></div>
   <section><h3>오늘의 조언</h3><p>{card.study.advice}</p></section><section><h3>이건 조심해!</h3><p>{card.study.caution}</p></section>
   <section className="study-tarot-mission"><h3>오늘의 미션</h3><p>{card.study.mission}</p></section>
   <section className="study-tarot-symbols"><h3>카드의 핵심 상징</h3><p>{card.symbols.join(" · ")}</p></section>
  </article>}</div>
 </section></main>
}
function localDateKey(){const now=new Date();return `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,"0")}-${String(now.getDate()).padStart(2,"0")}`}
function getSavedDailyCard(){const saved=window.localStorage.getItem("daily-study-tarot");if(!saved)return null;try{const value=JSON.parse(saved) as {date:string;cardId:string};if(value.date!==localDateKey())return null;return tarotCards.find(item=>item.id===value.cardId)??null}catch{window.localStorage.removeItem("daily-study-tarot");return null}}
