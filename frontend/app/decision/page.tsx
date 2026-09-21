"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import { Header } from "@/components/Header";

type Choice = "a" | "b";

export default function DecisionPage() {
  const [optionA, setOptionA] = useState("");
  const [optionB, setOptionB] = useState("");
  const [result, setResult] = useState<Choice | null>(null);
  const [drawing, setDrawing] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const scaleRef = useRef<HTMLDivElement>(null);

  useEffect(() => () => {
    if (timer.current) clearTimeout(timer.current);
  }, []);

  function resetResult() {
    if (timer.current) clearTimeout(timer.current);
    setDrawing(false);
    setResult(null);
  }

  function draw(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!optionA.trim() || !optionB.trim() || drawing) return;
    setResult(null);
    setDrawing(true);
    requestAnimationFrame(() => scaleRef.current?.scrollIntoView({ behavior: "smooth", block: "center" }));
    const winner: Choice = crypto.getRandomValues(new Uint32Array(1))[0] % 2 === 0 ? "a" : "b";
    timer.current = setTimeout(() => {
      setResult(winner);
      setDrawing(false);
      timer.current = null;
    }, 1800);
  }

  return <main className="app-shell content-shell decision-shell">
    <Header title="운명저울" />
    <section className="decision-content">
      <div className="decision-intro">
        <div className="decision-guide"><img src="/images/mascot-thinking.png" alt="선택지를 고민하는 여우 마스코트" /></div>
        <h1>운명저울</h1>
        <p>두 가지 선택을 저울에 올려보세요.</p>
      </div>
      <form onSubmit={draw} className="decision-form">
        <label htmlFor="decision-a">첫 번째 선택</label>
        <input id="decision-a" value={optionA} onChange={event => { setOptionA(event.target.value); resetResult(); }} placeholder="예: 도서관 가기" maxLength={40} required />
        <div className="decision-or">또는</div>
        <label htmlFor="decision-b">두 번째 선택</label>
        <input id="decision-b" value={optionB} onChange={event => { setOptionB(event.target.value); resetResult(); }} placeholder="예: 카페 가기" maxLength={40} required />
        <button className="decision-draw" type="submit" disabled={drawing || !optionA.trim() || !optionB.trim()}>{drawing ? "저울이 기우는 중…" : result ? "다시 올리기" : "운에 맡기기"}</button>
      </form>
      <div ref={scaleRef} className={`decision-scale${drawing ? " is-drawing" : result ? ` is-${result}` : ""}`} aria-hidden="true">
        <svg viewBox="0 0 520 350" role="presentation" preserveAspectRatio="xMidYMid meet">
          <path className="scale-star" d="M260 7c2 22 10 31 31 33-21 2-29 11-31 33-2-22-10-31-31-33 21-2 29-11 31-33Z" />
          <path className="scale-column" d="M255 81h10l7 249h-24l7-249Z" />
          <path className="scale-foot" d="M187 330h146l-20 19H207l-20-19Z" />
          <g className="scale-balance">
            <path className="scale-line" d="M70 80h380" />
            <g className="scale-pan-a">
              <path className="scale-bowl-fill" d="M16 226Q70 270 124 226Z" />
              <path className="scale-line" d="M70 80 16 226M70 80l54 146M16 226q54 44 108 0" />
              <foreignObject x="25" y="193" width="90" height="36"><div className="scale-label">{optionA.trim() || "첫 번째 선택"}</div></foreignObject>
            </g>
            <g className="scale-pan-b">
              <path className="scale-bowl-fill" d="M396 226q54 44 108 0Z" />
              <path className="scale-line" d="M450 80 396 226M450 80l54 146M396 226q54 44 108 0" />
              <foreignObject x="405" y="193" width="90" height="36"><div className="scale-label">{optionB.trim() || "두 번째 선택"}</div></foreignObject>
            </g>
          </g>
          <circle className="scale-pivot" cx="260" cy="80" r="10" />
        </svg>
      </div>
      <div className="decision-result" aria-live="polite" aria-atomic="true">
        {drawing ? <p>저울이 어느 쪽으로 기울까요?</p> : result ? <>
          <strong>{result === "a" ? optionA.trim() : optionB.trim()}</strong>
          <p>저울이 이쪽으로 기울었어요.</p>
        </> : <p>선택지를 적고 저울에 올려보세요.</p>}
      </div>
    </section>
  </main>;
}
