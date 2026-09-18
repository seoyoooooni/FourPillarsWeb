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
    todayTenGod: string;
  };
};

type CampusMetricKey = "study" | "relationships" | "health";

const campusFields: [CampusMetricKey, string, string][] = [
  ["study", "학업운", "수업과 과제에 집중하기 좋은 정도예요."],
  ["relationships", "관계운", "친구와 팀원 사이의 흐름을 보여줘요."],
  ["health", "컨디션", "오늘 활동할 수 있는 에너지의 흐름이에요."],
];

export default function CampusPage() {
  const router = useRouter();
  const [data, setData] = useState<CampusFortune>();
  const [error, setError] = useState("");
  const todayKey = localDateKey();

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
          <span className="result-meta">{new Intl.DateTimeFormat("ko-KR", { month: "long", day: "numeric", weekday: "long" }).format(new Date())}</span>
          <p>오늘의 캠퍼스 지수</p>
          <strong>{data.fortune.overall}</strong><em>점</em>
          <small>{campusMessage(data.fortune.overall, todayKey)}</small>
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
          <p>{dailyTip(data.fortune, todayKey)}</p>
        </article>
      </section>}
  </main>;
}

function campusMessage(score: number, dateKey: string) {
  const messages = score >= 80 ? [
    "해보고 싶던 일에 먼저 손을 들어봐도 좋은 날이에요.",
    "작은 도전이 기대보다 좋은 반응으로 돌아올 수 있어요.",
    "주도적으로 움직일수록 캠퍼스의 흐름이 따라오는 날이에요.",
    "오늘은 망설이기보다 한 걸음 먼저 나서보세요.",
  ] : score >= 70 ? [
    "중요한 일부터 차근차근 시작하면 흐름을 타기 좋아요.",
    "집중할 곳을 하나 정하면 만족스러운 하루가 될 수 있어요.",
    "준비한 만큼 결과가 따라오기 쉬운 안정적인 날이에요.",
    "평소보다 조금 적극적으로 움직여도 좋은 날이에요.",
  ] : score >= 60 ? [
    "평소의 리듬을 지키면 무난하게 풀리는 날이에요.",
    "욕심내기보다 계획한 일을 마치는 데 집중해 보세요.",
    "크게 서두르지 않아도 필요한 일은 제자리를 찾아가요.",
    "익숙한 루틴이 오늘의 든든한 힘이 되어줄 거예요.",
  ] : [
    "서두르기보다 내 페이스를 챙기는 편이 좋아요.",
    "할 일을 조금 덜어내고 꼭 필요한 것부터 챙겨보세요.",
    "잠깐 멈춰 호흡을 고르면 흐름을 다시 잡을 수 있어요.",
    "성과보다 컨디션 관리에 무게를 두어도 괜찮은 날이에요.",
  ];
  return pick(messages, `${dateKey}-${score}`);
}

function dailyTip(fortune: CampusFortune["fortune"], dateKey: string) {
  const ranked = campusFields
    .map(([key]) => ({ key, score: fortune[key] as number }))
    .sort((a, b) => b.score - a.score);
  const best = ranked[0];
  const weakest = ranked[ranked.length - 1];
  const gap = best.score - weakest.score;
  const seed = `${dateKey}-${fortune.overall}-${fortune.todayTenGod}-${best.key}-${weakest.key}`;

  if (gap <= 3) {
    return pick([
      "학업과 약속, 휴식의 균형이 고른 날이에요. 한 가지에 힘을 다 쓰기보다 일정 사이에 여백을 남겨보세요.",
      "어느 한쪽으로 치우치지 않는 흐름이에요. 수업과 사람, 내 컨디션을 비슷한 비중으로 챙겨보세요.",
      "전체 흐름이 안정적이에요. 새로운 계획을 늘리기보다 정해둔 일들을 깔끔하게 마무리해 보세요.",
      "오늘은 균형 감각이 장점이에요. 집중한 뒤 쉬고, 혼자 한 뒤 사람들과 나누는 리듬을 만들어보세요.",
    ], seed);
  }

  const strengthMessages: Record<CampusMetricKey, string[]> = {
    study: [
      "이해력과 집중력이 받쳐주는 날이에요. 미뤄둔 과제의 첫 문단부터 가볍게 시작해 보세요.",
      "학업운이 가장 선명해요. 어려운 과목이나 오래 미룬 복습을 먼저 처리하면 진도가 잘 나갈 수 있어요.",
      "머릿속 생각을 정리하기 좋은 날이에요. 강의가 끝난 뒤 핵심 세 줄만 적어두어도 오래 남아요.",
      "혼자 몰입하는 시간이 성과로 이어지기 쉬워요. 알림을 잠시 끄고 짧게 집중해 보세요.",
      "배운 내용을 다른 사람에게 설명해 보세요. 막혔던 부분이 의외로 쉽게 정리될 수 있어요.",
    ],
    relationships: [
      "사람을 통해 실마리를 얻기 좋은 날이에요. 동기나 선배에게 먼저 가볍게 말을 건네보세요.",
      "관계운이 가장 좋아요. 혼자 고민하던 일은 믿을 만한 사람과 나누면 생각이 한결 선명해질 수 있어요.",
      "팀 활동에서 자연스럽게 분위기를 잇는 역할을 할 수 있어요. 짧은 안부가 좋은 대화로 이어질 거예요.",
      "새로운 인연보다 이미 가까운 사람을 챙겨보세요. 사소한 배려가 오래 기억되는 날이에요.",
      "의견을 먼저 묻는 태도가 도움이 돼요. 함께 결정하면 혼자 밀어붙일 때보다 결과가 좋아질 수 있어요.",
    ],
    health: [
      "컨디션이 오늘의 강점이에요. 미뤄둔 외부 일정이나 몸을 움직이는 일을 처리해 보세요.",
      "활동 에너지가 비교적 좋은 날이에요. 오래 앉아 있기보다 이동과 집중을 번갈아 해보세요.",
      "몸의 리듬이 잘 받쳐줘요. 수업 전 짧게 걷거나 스트레칭하면 좋은 흐름을 더 오래 유지할 수 있어요.",
      "일정을 소화할 힘은 충분해요. 다만 한꺼번에 몰아서 쓰지 말고 중간중간 물과 식사를 챙겨보세요.",
      "가벼운 운동이나 산책이 머리까지 맑게 해줄 수 있어요. 빈 시간 10분을 움직임에 써보세요.",
    ],
  };

  const cautionMessages: Record<CampusMetricKey, string[]> = {
    study: [
      "다만 집중이 흐려질 수 있으니 할 일을 20분 단위로 잘게 나누는 편이 좋아요.",
      "학업은 욕심을 줄이고 가장 급한 한 가지를 끝내는 데 초점을 맞춰보세요.",
      "복잡한 공부는 오전이나 컨디션이 좋은 시간대로 당겨두는 것이 좋겠어요.",
    ],
    relationships: [
      "다만 말이 엇갈리기 쉬우니 중요한 내용은 메시지보다 직접 확인해 보세요.",
      "사람 사이에서는 바로 답하기보다 한 번 듣고 정리한 뒤 말하는 편이 좋아요.",
      "모든 부탁을 받아주기보다 오늘 가능한 범위를 분명하게 알려주세요.",
    ],
    health: [
      "다만 체력이 뒤늦게 떨어질 수 있으니 빈 시간마다 짧게 쉬어가는 것이 좋아요.",
      "컨디션은 과신하지 말고 식사와 수분, 귀가 시간을 미리 챙겨두세요.",
      "일정을 빽빽하게 채우기보다 다음 일정 전 10분의 여유를 남겨보세요.",
    ],
  };

  const strength = pick(strengthMessages[best.key], `${seed}-strength`);
  if (weakest.score <= 60 || gap >= 8) {
    return `${strength} ${pick(cautionMessages[weakest.key], `${seed}-caution`)}`;
  }
  return strength;
}

function pick<T>(items: T[], seed: string) {
  let hash = 0;
  for (const character of seed) hash = (hash * 31 + character.charCodeAt(0)) | 0;
  return items[Math.abs(hash) % items.length];
}

function localDateKey() {
  const now = new Date();
  return `${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}`;
}
