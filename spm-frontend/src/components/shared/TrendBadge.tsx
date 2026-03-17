import { useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { Badge } from 'flowbite-react';
import type { Trend } from '../../types/domain';

const trendDescriptions: Record<Trend, string> = {
  IMPROVING: 'Recent tests average more than 2 percentage points above earlier tests.',
  DECLINING: 'Recent tests average more than 2 percentage points below earlier tests.',
  STABLE: 'The difference between recent and earlier test averages is within 2 percentage points.',
  INSUFFICIENT_DATA: 'Fewer than 2 tests recorded — not enough data to determine a trend.',
};

interface Props {
  trend: Trend;
  size?: 'sm' | 'xs';
  className?: string;
}

export function TrendBadge({ trend, size = 'sm', className }: Props) {
  const color = trend === 'IMPROVING' ? 'success' : trend === 'DECLINING' ? 'failure' : 'gray';
  const ref = useRef<HTMLSpanElement>(null);
  const [pos, setPos] = useState<{ top: number; left: number } | null>(null);

  const show = () => {
    if (!ref.current) return;
    const rect = ref.current.getBoundingClientRect();
    const tipW = 260;
    let left = rect.left + rect.width / 2 - tipW / 2;
    // keep within viewport horizontally
    if (left < 8) left = 8;
    if (left + tipW > window.innerWidth - 8) left = window.innerWidth - tipW - 8;
    // prefer above; if not enough room, go below
    const above = rect.top - 8;
    const below = rect.bottom + 8;
    const top = above > 80 ? above : below;
    setPos({ top, left });
  };

  const hide = () => setPos(null);

  return (
    <>
      <span
        ref={ref}
        className={`inline-block ${className ?? ''}`}
        onMouseEnter={show}
        onMouseLeave={hide}
      >
        <Badge color={color} size={size}>{trend}</Badge>
      </span>
      {pos && createPortal(
        <div
          role="tooltip"
          style={{
            position: 'fixed',
            top: pos.top > (ref.current?.getBoundingClientRect().top ?? 0)
              ? pos.top
              : undefined,
            bottom: pos.top <= (ref.current?.getBoundingClientRect().top ?? 0)
              ? `${window.innerHeight - pos.top}px`
              : undefined,
            left: pos.left,
            width: 260,
            zIndex: 9999,
          }}
          className="rounded-lg bg-gray-900 px-3 py-2 text-xs text-white shadow-lg dark:bg-gray-700"
        >
          {trendDescriptions[trend]}
        </div>,
        document.body,
      )}
    </>
  );
}
