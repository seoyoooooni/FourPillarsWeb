export const STEM_ELEMENT: Record<string, string> = { 갑: "목", 을: "목", 병: "화", 정: "화", 무: "토", 기: "토", 경: "금", 신: "금", 임: "수", 계: "수" };
export const GENERATES: Record<string, string> = { 목: "화", 화: "토", 토: "금", 금: "수", 수: "목" };
export const CONTROLS: Record<string, string> = { 목: "토", 토: "수", 수: "화", 화: "금", 금: "목" };
export const BRANCH_COMBINATIONS = new Set(["자축", "인해", "묘술", "진유", "사신", "오미"]);
export const BRANCH_CLASHES = new Set(["자오", "축미", "인신", "묘유", "진술", "사해"]);
export const BRANCH_HARMS = new Set(["자미", "축오", "인사", "묘진", "신해", "유술"]);
