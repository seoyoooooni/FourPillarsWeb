export type Profile = { displayName: string; birthDate: string; birthTime: string; calendarType: "SOLAR" | "LUNAR"; leapMonth: boolean; gender: "MALE" | "FEMALE" };
export type Calculation = {
  solarDate: string;
  lunarDate: { year: number; month: number; day: number; leapMonth: boolean };
  pillars: Record<string, string>;
  analysis: { dayMaster: string; tenGods: Record<string, string>; hiddenStems: Record<string, { stem: string; tenGod: string; position: string }[]>; elementCounts: Record<string, number> };
  stageTwoAnalysis: { twelveStages: Record<string, string>; gongmang: string[]; twelveSinsalByYearBranch: Record<string, string>; twelveSinsalByDayBranch: Record<string, string> };
  stageThreeAnalysis: { cheondeokValue: string; gilsinByPillar: Record<string, string[]>; hyungsalByPillar: Record<string, string[]>; wonjinPairs: { first: string; second: string }[] };
  yongsinAnalysis: { strengthScore: number; strength: string; categoryCounts: Record<string, number>; yongsinElement: string; yongsinReason: string; weakPriority: string; strongPriority: string };
  daeunAnalysis: { forward: boolean; startAge: number; periods: { order: number; startAge: number; stem: string; branch: string; stemTenGod: string; branchTenGod: string; twelveStage: string }[] } | null;
};
export type MajorRecommendation = {
  studentType: string;
  traits: Record<string, number>;
  aptitudes: Record<string, number>;
  recommendations: {
    role: string;
    department: string;
    score: number;
    reasons: string[];
    sourceUrl: string;
    admissionNote: string | null;
  }[];
  disclaimer: string;
};

const tokenKey = "fourpillars_access_token";
const refreshKey = "fourpillars_refresh_token";

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.message ?? "요청을 처리하지 못했습니다.");
  }
  return response.status === 204 ? (undefined as T) : response.json();
}

export async function api<T>(path: string, init: RequestInit = {}, auth = false, retried = false): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body) headers.set("Content-Type", "application/json");
  if (auth && typeof window !== "undefined") {
    const token = localStorage.getItem(tokenKey);
    if (token) headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(`/backend${path}`, { ...init, headers });
  if (auth && response.status === 401 && !retried && await refreshAccessToken()) {
    return api<T>(path, init, true, true);
  }
  if (auth && response.status === 401) {
    logout();
    throw new Error("로그인이 만료되었습니다. 다시 로그인해 주세요.");
  }
  return parse<T>(response);
}

async function refreshAccessToken() {
  if (typeof window === "undefined") return false;
  const refreshToken = localStorage.getItem(refreshKey);
  if (!refreshToken) return false;
  try {
    const response = await fetch("/backend/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (!response.ok) return false;
    const tokens = await response.json() as { accessToken: string; refreshToken: string };
    localStorage.setItem(tokenKey, tokens.accessToken);
    localStorage.setItem(refreshKey, tokens.refreshToken);
    return true;
  } catch {
    return false;
  }
}

export async function login(loginId: string, password: string) {
  const tokens = await api<{ accessToken: string; refreshToken: string }>("/api/auth/login", { method: "POST", body: JSON.stringify({ loginId, password }) });
  localStorage.setItem(tokenKey, tokens.accessToken); localStorage.setItem(refreshKey, tokens.refreshToken);
}
export async function signup(loginId: string, password: string) { await api("/api/auth/signup", { method: "POST", body: JSON.stringify({ loginId, password }) }); }
export function logout() { localStorage.removeItem(tokenKey); localStorage.removeItem(refreshKey); }
export function hasToken() { return typeof window !== "undefined" && Boolean(localStorage.getItem(tokenKey)); }
