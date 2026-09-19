import { ReactNode, useId } from "react";

export function HandDrawnBorder({ className, children }: { className?: string; children: ReactNode }) {
  const filterId = useId();
  return <svg className={className} viewBox="0 0 100 100" preserveAspectRatio="none" aria-hidden="true">
    <defs>
      <filter id={filterId} x="-20%" y="-20%" width="140%" height="140%">
        <feTurbulence type="fractalNoise" baseFrequency="0.72" numOctaves="2" seed="7" result="noise" />
        <feDisplacementMap in="SourceGraphic" in2="noise" scale="0.9" xChannelSelector="R" yChannelSelector="G" />
      </filter>
    </defs>
    <g style={{ filter: `url(#${filterId})` }}>{children}</g>
  </svg>;
}
