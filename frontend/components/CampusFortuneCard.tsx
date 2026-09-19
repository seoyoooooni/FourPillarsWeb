import { campusDailyTip, campusFields, CampusFortune } from "@/lib/campus-fortune";
import { HandDrawnBorder } from "@/components/HandDrawnBorder";

export function CampusFortuneCard({ fortune, dateKey }: { fortune: CampusFortune; dateKey: string }) {
  return <div className="campus-card">
    <HandDrawnBorder className="campus-hand-border">
      <path d="M3.2 0 C18 0.7 33 0.2 48.8 0.9 C66 0.3 80 1 96 0.2 C98.73 1.02 99.79 3.88 99.36 8.16 C100 27.55 98.73 43.88 99.26 61.22 C99.68 75.51 98.51 88.78 98.94 95.41 C99.04 98.16 97.56 99.39 94.69 99.18 C78.56 98.57 65.82 100 49.89 99.29 C33.97 100 20.17 98.78 4.46 99.39 C1.7 99.49 0.42 97.65 0.64 94.69 C0 78.57 1.27 63.27 0.64 47.96 C0.21 32.65 1.27 19.39 0.53 7.86 C0.3 3.6 1.5 0.6 3.2 0 Z" />
    </HandDrawnBorder>
    {campusFields.map(([key, label]) => <div className="campus-metric" key={key}>
      <div className="campus-metric-row">
        <span className="campus-metric-label">{label}</span>
        <strong className="campus-metric-score">{fortune[key]}</strong>
      </div>
      <i className="campus-metric-bar"><b style={{ width: `${fortune[key]}%` }} /></i>
    </div>)}
    <p className="campus-tip">{campusDailyTip(fortune, dateKey)}</p>
  </div>;
}
