"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Header } from "@/components/Header";
import { api, hasToken } from "@/lib/api";

type CampusFortune = {
  fortune: {
    overall: number;
    health: number;
    relationships: number;
    study: number;
  };
};

const campusFields: [keyof CampusFortune["fortune"], string, string][] = [
  ["study", "학업운", "수업과 과제에 집중하기 좋은 정도예요."],
  ["relationships", "관계운", "친구와 팀원 사이의 흐름을 보여줘요."],
  ["health", "컨디션", "오늘 활동할 수 있는 에너지의 흐름이에요."],
];

export default function CampusPage() {
  const router = useRouter();
  const [data, setData] = useState<CampusFortune>();
  const [error, setError] = useState("");

  useEffect(() => {
    if (!hasToken()) {
      router.replace("/login");
      return;
    }
    api<CampusFortune>("/api/fortune/today", {}, true).then(setData).catch((e) => setError(e.message));
  }, [router]);

  return <main className="app-shell content-shell">
    <Header title="오늘의 캠퍼스 운세" />
    {error ? <section className="empty-state"><p>{error}</p><Link className="primary link-button" href="/profile">설정하기</Link></section>
      : !data ? <div className="loading">오늘의 캠퍼스 흐름을 살펴보는 중…</div>
      : <section className="campus-result">
        <div className="campus-hero">
          <span>{new Intl.DateTimeFormat("ko-KR", { month: "long", day: "numeric", weekday: "long" }).format(new Date())}</span>
          <p>오늘의 캠퍼스 지수</p>
          <strong>{data.fortune.overall}</strong><em>점</em>
          <small>{campusMessage(data.fortune.overall)}</small>
        </div>
        <div className="campus-fortunes">
          {campusFields.map(([key, label, description]) => <article key={key}>
            <div><h2>{label}</h2><strong>{data.fortune[key]}</strong></div>
            <i><b style={{ width: `${data.fortune[key]}%` }} /></i>
            <p>{description}</p>
          </article>)}
        </div>
        <article className="campus-tip">
          <h2>오늘의 한마디</h2>
          <p>{dailyTip(data.fortune)}</p>
        </article>
      </section>}
  </main>;
}

function campusMessage(score: number) {
  if (score >= 80) return "새로운 일에 먼저 손을 들어봐도 좋은 날이에요.";
  if (score >= 60) return "평소의 리듬을 지키면 무난하게 풀리는 날이에요.";
  return "서두르기보다 내 페이스를 챙기는 편이 좋아요.";
}

function dailyTip(fortune: CampusFortune["fortune"]) {
  const best = campusFields.reduce((a, b) => fortune[a[0]] >= fortune[b[0]] ? a : b);
  if (best[0] === "study") return "미뤄둔 과제나 복습을 시작해 보세요. 생각보다 빠르게 진도가 나갈 수 있어요.";
  if (best[0] === "relationships") return "혼자 고민하던 일이 있다면 동기나 선배에게 가볍게 말을 건네보세요.";
  return "일정 사이에 잠깐이라도 쉬어가세요. 컨디션을 잘 쓰는 것이 오늘의 핵심이에요.";
}
