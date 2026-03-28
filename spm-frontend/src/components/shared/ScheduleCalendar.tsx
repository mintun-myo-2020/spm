import { useState, useMemo } from 'react';
import { Button } from 'flowbite-react';
import { HiChevronLeft, HiChevronRight } from 'react-icons/hi';
import type { SessionDTO } from '../../types/domain';

interface ScheduleCalendarProps {
  sessions: SessionDTO[];
  onDateClick: (date: string) => void;
  selectedDate: string | null;
}

const DAY_NAMES = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export function ScheduleCalendar({ sessions, onDateClick, selectedDate }: ScheduleCalendarProps) {
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });

  const sessionsByDate = useMemo(() => {
    const map = new Map<string, SessionDTO[]>();
    for (const s of sessions) {
      const existing = map.get(s.sessionDate) ?? [];
      existing.push(s);
      map.set(s.sessionDate, existing);
    }
    return map;
  }, [sessions]);

  const calendarDays = useMemo(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const days: (number | null)[] = [];
    for (let i = 0; i < firstDay; i++) days.push(null);
    for (let d = 1; d <= daysInMonth; d++) days.push(d);
    return days;
  }, [currentMonth]);

  const formatDate = (day: number) => {
    const y = currentMonth.getFullYear();
    const m = String(currentMonth.getMonth() + 1).padStart(2, '0');
    const d = String(day).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  const getStatusColor = (daySessions: SessionDTO[]) => {
    const hasScheduled = daySessions.some(s => s.status === 'SCHEDULED');
    const hasCancelled = daySessions.some(s => s.status === 'CANCELLED');
    const hasCompleted = daySessions.some(s => s.status === 'COMPLETED');
    if (hasScheduled) return 'bg-blue-500';
    if (hasCompleted) return 'bg-green-500';
    if (hasCancelled) return 'bg-red-400';
    return 'bg-gray-400';
  };

  const monthLabel = currentMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 dark:border-gray-700 dark:bg-gray-800" data-testid="schedule-calendar">
      <div className="mb-4 flex items-center justify-between">
        <Button size="xs" color="gray" onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1))} data-testid="calendar-prev">
          <HiChevronLeft className="h-4 w-4" />
        </Button>
        <span className="text-sm font-semibold text-gray-700 dark:text-gray-300">{monthLabel}</span>
        <Button size="xs" color="gray" onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1))} data-testid="calendar-next">
          <HiChevronRight className="h-4 w-4" />
        </Button>
      </div>
      <div className="grid grid-cols-7 gap-1">
        {DAY_NAMES.map(d => (
          <div key={d} className="py-1 text-center text-xs font-medium text-gray-500 dark:text-gray-400">{d}</div>
        ))}
        {calendarDays.map((day, i) => {
          if (day === null) return <div key={`empty-${i}`} />;
          const dateStr = formatDate(day);
          const daySessions = sessionsByDate.get(dateStr);
          const isSelected = dateStr === selectedDate;
          const isToday = dateStr === new Date().toISOString().split('T')[0];
          return (
            <button
              key={dateStr}
              type="button"
              onClick={() => onDateClick(dateStr)}
              className={`relative flex h-10 items-center justify-center rounded-md text-sm transition-colors
                ${isSelected ? 'bg-blue-100 font-bold text-blue-700 dark:bg-blue-900 dark:text-blue-300' : 'hover:bg-gray-100 dark:hover:bg-gray-700'}
                ${isToday && !isSelected ? 'font-semibold text-blue-600 dark:text-blue-400' : 'text-gray-700 dark:text-gray-300'}`}
              data-testid={`calendar-day-${dateStr}`}
            >
              {day}
              {daySessions && (
                <span className={`absolute bottom-1 h-1.5 w-1.5 rounded-full ${getStatusColor(daySessions)}`} />
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
