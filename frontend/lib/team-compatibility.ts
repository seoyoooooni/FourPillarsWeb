import type { Calculation } from "./api";
import { STEM_ELEMENT, GENERATES, CONTROLS, BRANCH_COMBINATIONS, BRANCH_CLASHES, BRANCH_HARMS } from "./five-elements";

export type PairResult = { first: string; second: string; score: number; summary: string };
export type TeamResult = { score: number; title: string; pairs: PairResult[]; roles: { name: string; role: string }[]; tip: string };

const ROLE_BY_ELEMENT: Record<string, string> = { 목: "기획·방향 설정", 화: "발표·분위기 전환", 토: "일정·의견 조율", 금: "자료 정리·최종 검수", 수: "조사·아이디어 확장" };

export function analyzeTeam(names: string[], calculations: Calculation[]): TeamResult {
  const pairs: PairResult[] = [];
  for (let first = 0; first < names.length; first++) for (let second = first + 1; second < names.length; second++) pairs.push(analyzePair(names[first], calculations[first], names[second], calculations[second]));
  const score = Math.round(pairs.reduce((sum, pair) => sum + pair.score, 0) / pairs.length);
  const roles = calculations.map((calculation, index) => ({ name: names[index], role: roleFor(calculation) }));
  const title = score >= 90 ? "서로의 빈틈을 채우는 좋은 팀" : score >= 80 ? "역할을 나누면 강해지는 팀" : score >= 70 ? "규칙을 정하면 편안한 팀" : "속도를 맞추는 연습이 필요한 팀";
  const tip = score >= 85 ? "서로 다른 강점을 믿고 역할을 분명히 나누면 완성도와 속도를 함께 잡을 수 있어요." : score >= 72 ? "회의 시작 전에 결정 방식과 마감 기준을 정하면 의견 차이가 팀의 장점으로 바뀔 수 있어요." : "중간 점검 시간을 짧게 자주 만들고, 중요한 결정은 말보다 문서로 남기는 편이 좋아요.";
  return { score, title, pairs: pairs.sort((a, b) => b.score - a.score), roles, tip };
}

function analyzePair(firstName: string, first: Calculation, secondName: string, second: Calculation): PairResult {
  const firstDay = Array.from(first.pillars.day ?? ""); const secondDay = Array.from(second.pillars.day ?? "");
  const firstElement = STEM_ELEMENT[firstDay[0]]; const secondElement = STEM_ELEMENT[secondDay[0]];
  let score = 72; const reasons: string[] = [];
  if (firstElement === secondElement) { score += 5; reasons.push("기본적으로 일하는 결이 비슷해요"); }
  else if (GENERATES[firstElement] === secondElement || GENERATES[secondElement] === firstElement) { score += 12; reasons.push("서로의 오행을 자연스럽게 북돋는 관계예요"); }
  else if (CONTROLS[firstElement] === secondElement || CONTROLS[secondElement] === firstElement) { score -= 4; reasons.push("속도와 판단 기준이 다를 수 있어요"); }
  if (hasBranchRelation(BRANCH_COMBINATIONS, firstDay[1], secondDay[1])) { score += 12; reasons.push("일지의 합이 있어 호흡을 맞추기 좋아요"); }
  if (hasBranchRelation(BRANCH_CLASHES, firstDay[1], secondDay[1])) { score -= 12; reasons.push("의견이 정면으로 부딪힐 때가 있어요"); }
  else if (hasBranchRelation(BRANCH_HARMS, firstDay[1], secondDay[1])) { score -= 6; reasons.push("작은 오해는 바로 확인하는 편이 좋아요"); }
  if (first.yongsinAnalysis.yongsinElement === secondElement || second.yongsinAnalysis.yongsinElement === firstElement) { score += 7; reasons.push("한 사람의 부족한 기운을 다른 사람이 보완해요"); }
  const balance = elementComplement(first.analysis.elementCounts, second.analysis.elementCounts); score += balance;
  if (balance >= 5) reasons.push("두 사람의 오행 분포가 서로의 빈틈을 채워줘요");
  score = Math.max(45, Math.min(98, score));
  return { first: firstName, second: secondName, score, summary: `${(reasons.length ? reasons : ["큰 충돌 없이 무난하게 역할을 맞출 수 있어요"]).slice(0, 2).join(". ")}.` };
}

function elementComplement(first: Record<string, number>, second: Record<string, number>) {
  let complement = 0;
  for (const element of ["목", "화", "토", "금", "수"]) if (((first[element] ?? 0) <= 1 && (second[element] ?? 0) >= 2) || ((second[element] ?? 0) <= 1 && (first[element] ?? 0) >= 2)) complement++;
  return Math.min(7, complement * 2);
}
function roleFor(calculation: Calculation) { const strongest = Object.entries(calculation.analysis.elementCounts).sort((a, b) => b[1] - a[1])[0]?.[0] ?? "토"; return ROLE_BY_ELEMENT[strongest] ?? ROLE_BY_ELEMENT.토; }
function hasBranchRelation(relations: Set<string>, first?: string, second?: string) {
  if (!first || !second) return false;
  return relations.has(first + second) || relations.has(second + first);
}
